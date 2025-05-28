package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.relation_binder.delegates.RelationBindingDelegate
import po.exposify.dto.interfaces.ComponentType
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.lastTaskHandler
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.interfaces.Identifiable
import po.misc.reflection.properties.PropertyMap
import po.misc.registries.type.TypeRegistry
import po.misc.serialization.SerializerInfo
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KType
import kotlin.reflect.full.companionObjectInstance


inline fun <reified E : LongEntity, reified EX : ManagedException> getExposifyEntityCompanion(): ExposifyEntityClass<E> {
    val companion = E::class.companionObjectInstance
        ?: throw SelfThrownException.build<EX>("Missing companion object for ${E::class.simpleName}", 0)
    val base = companion.castBaseOrThrow<ExposifyEntityClass<*>, EX>()
    return base.castOrThrow<ExposifyEntityClass<E>, InitException>()
}


inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.configuration(
    noinline block:  DTOConfig<DTO, D, E>.() -> Unit
): Unit where DTO: ModelDTO, D: DataModel, E: LongEntity {

    val registry = TypeRegistry()
    val dtoTypeRec = registry.addRecord<DTO>(ComponentType.DTO)
    val dataTypeRec = registry.addRecord<D>(ComponentType.DATA_MODEL)
    val entityTypeRec = registry.addRecord<E>(ComponentType.ENTITY)

    val propertyMap = PropertyMap()
    propertyMap.applyClass(ComponentType.DATA_MODEL, dataTypeRec.clazz, dataTypeRec)
    propertyMap.applyClass(ComponentType.ENTITY, entityTypeRec.clazz, entityTypeRec)

    val entityModel =  getExposifyEntityCompanion<E, InitException>()
    val newConfiguration = DTOConfig(registry, propertyMap, entityModel, this)
    configParameter = newConfiguration
    block.invoke(config)
    initialized = true
}



sealed class DTOBase<DTO, DATA, ENTITY>(): ClassDTO, TasksManaged, Identifiable
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity
{
    @PublishedApi
    internal var configParameter: DTOConfig<DTO, DATA, ENTITY>? = null
    val config:  DTOConfig<DTO, DATA, ENTITY>
        get() = configParameter.getOrInitEx("DTOConfig uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)

    override var initialized: Boolean = false
    abstract override val qualifiedName: String

    internal val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    protected abstract fun  setup()

//    inline fun <reified COMMON,  reified RD, reified RE> configuration(
//
//        entityModel: ExposifyEntityClass<RE>,
//        noinline block:  DTOConfig<DTO, DATA, ENTITY>.() -> Unit
//    ): Unit where COMMON: ModelDTO, RD: DataModel, RE: LongEntity
//            = runTask("DTO Configuration", TaskConfig(moduleName = qualifiedName)){
//        val newConfiguration = DTOConfig(DTORegistryItem(RD::class, RE::class, COMMON::class), entityModel, this.castOrInitEx())
//        configParameter = newConfiguration.castOrInitEx()
//        block.invoke(config)
//        //  forceHandlerProviderResolution(this)
//        initialized = true
//    }.resultOrException()

    internal fun registerDTO(dto: CommonDTO<DTO, DATA, ENTITY>){
        val existed = dtoMap.containsKey(dto.id)
        dtoMap[dto.id] = dto
        if (existed) {
            val handler = lastTaskHandler()
            handler.warn("Given dto with id: ${dto.id} already exist in dtoMap")
        }
    }

    internal fun lookupDTO(id: Long): CommonDTO<DTO, DATA, ENTITY>?{
        return dtoMap[id]
    }

    override fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(config.entityModel.table)
        config.relationBinder.getChildClassList().forEach {
            it.getAssociatedTables(cumulativeList)
        }
    }

    fun getEntityModel(): ExposifyEntityClass<ENTITY> {
        return config.entityModel.castOrOperationsEx<ExposifyEntityClass<ENTITY>>()
    }

    fun <DTO: ModelDTO, D: DataModel, E: LongEntity> findHierarchyRoot(): RootDTO<DTO, D, E>{
        return when(this){
            is RootDTO->{
                this.castOrInitEx()
            }

            is DTOClass->{
                parentClass.findHierarchyRoot()
            }
        }
    }

    fun whereQuery(): WhereQuery<IdTable<Long>> {
        return  WhereQuery(config.entityModel.sourceTable)
    }

    fun serializerLookup(type: KType): SerializerInfo<*>?{
        val normalizedKey  = type.toSimpleNormalizedKey()
        when(this){
            is RootDTO->{
                val serializersMap = serviceContext.serviceClass.connectionClass.serializerMap
                return serializersMap[normalizedKey]
            }
            is DTOClass ->{
                findHierarchyRoot<ModelDTO, DataModel, LongEntity>().let {hierarchyRoot->
                    val serializersMap =  hierarchyRoot.serviceContext.serviceClass.connectionClass.serializerMap
                    return serializersMap[normalizedKey]
                }
            }
        }
    }
}

abstract class RootDTO<DTO, DATA, ENTITY>()
    : DTOBase<DTO, DATA, ENTITY>(),  TasksManaged,  ClassDTO
        where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity
{
    override val qualifiedName: String
        get() = configParameter?.registry?.getSimpleName(ComponentType.DTO)?:"RootDTO[Uninitialized]"

    private var serviceContextParameter: ServiceContext<DTO, DATA, ENTITY>? = null
    val serviceContext: ServiceContext<DTO, DATA, ENTITY>
        get() = serviceContextParameter.getOrInitEx()

    fun initialization(serviceContext: ServiceContext<DTO, DATA, ENTITY>) {
        serviceContextParameter = serviceContext
        if (!initialized) setup()
    }
    fun reinitChild(){
        config.relationBinder.getChildClassList().forEach {
            if(!it.initialized){
                it.initialization()
            }
        }
    }

    fun getServiceClass(): ServiceClass<DTO, DATA, ENTITY>{
       return serviceContext.serviceClass.getOrInitEx("ServiceClass not assigned for $qualifiedName")
    }

    fun switchQuery(id: Long): SwitchQuery<DTO, DATA, ENTITY> {
        return SwitchQuery(id, this)
    }

}

abstract class DTOClass<DTO, DATA, ENTITY>(
    val parentClass: DTOBase<*, *, *>,
): DTOBase<DTO, DATA, ENTITY>(), ClassDTO, TasksManaged
        where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity {

    override val qualifiedName: String
        get() = configParameter?.registry?.getSimpleName(ComponentType.DTO)?:"DTOClass[Uninitialized]"

    fun initialization() {
        if (!initialized){ setup() }
    }
}

