package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.bindings.BindingHub.ListData
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.task.TaskHandler
import po.lognotify.lastTaskHandler
import po.misc.callbacks.manager.CallbackManager
import po.misc.callbacks.manager.CallbackPayload
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableImplementation
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.asIdentifiableClass
import po.misc.registries.type.TypeRegistry
import po.misc.serialization.SerializerInfo
import po.misc.types.TypeRecord
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KType


sealed class DTOBase<DTO, DATA, ENTITY>(): ClassDTO,  TasksManaged,  InlineAction, IdentifiableClass
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity
{
    enum class Events(override val value: Int) : ValueBased{
        Initialized(1),
        StatusChanged(2),
        DelegateRegistrationComplete(11);
    }


    abstract override val identity: ClassIdentity<*>
    override val contextName: String get() = identity.componentName

    internal val notifier = CallbackManager(
        enumClass =  Events::class.java,
        emitter = this,
        CallbackPayload<Events, DTOBase<*,*,*>>(Events.Initialized),
        CallbackPayload<Events, DTOBase<*,*,*>>(Events.StatusChanged),
        CallbackPayload<Events, ListData<DTO, DATA, ENTITY>>(Events.DelegateRegistrationComplete)
    )

    var status: DTOClassStatus = DTOClassStatus.Undefined
        private set(value) {
            if(field != value){
                field = value
                notifier.trigger(Events.StatusChanged, this)
            }
        }

    @PublishedApi
    internal var configParameter: DTOConfig<DTO, DATA, ENTITY>? = null
    val config:  DTOConfig<DTO, DATA, ENTITY>
        get() {
          return  configParameter.getOrInitEx("DTOConfig uninitialized $completeName", ExceptionCode.LAZY_NOT_INITIALIZED)
        }

    override var initialized: Boolean = false
    protected val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    internal val logger : TaskHandler<*> get() = lastTaskHandler()

    private val registryExceptionMessage = "Can not find type record in DTOClass"
    private val registry:  TypeRegistry get()= config.registry
    internal val dtoType : TypeRecord<DTO>
        get() = registry.getRecord<DTO, InitException>(SourceObject.DTO, registryExceptionMessage)
    internal val dataType : TypeRecord<DATA>
        get() = registry.getRecord<DATA, InitException>(SourceObject.Data, registryExceptionMessage)
    internal val entityType : TypeRecord<ENTITY>
        get() = registry.getRecord<ENTITY, InitException>(SourceObject.Entity, registryExceptionMessage)

    @PublishedApi
    internal abstract fun initializationComplete()
    protected abstract fun  setup()

    @PublishedApi
    internal fun updateStatus(newStatus: DTOClassStatus){
        status = newStatus
    }

    internal fun registerDTO(dto: CommonDTO<DTO, DATA, ENTITY>){
        val existed = dtoMap.containsKey(dto.id)
        dtoMap[dto.id] = dto
        if (existed) {
            val handler = lastTaskHandler()
            handler.warn("Given dto with id: ${dto.id} already exist in dtoMap")
        }
    }

    internal fun clearCachedDTOs(){
        dtoMap.clear()
    }

    internal fun lookupDTO(id: Long): CommonDTO<DTO, DATA, ENTITY>?{
        return dtoMap[id]
    }
    internal fun lookupDTO(id: Long, operation: CrudOperation): CommonDTO<DTO, DATA, ENTITY>?{
        return dtoMap[id]?.addTrackerInfo(operation, this)
    }
    internal fun lookupDTO(): List<CommonDTO<DTO, DATA, ENTITY>>{
       return dtoMap.values.toList()
    }

    override fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(config.entityModel.table)
        config.childClasses.forEach {
            it.getAssociatedTables(cumulativeList)
        }
    }

    fun getEntityModel(): ExposifyEntityClass<ENTITY> {
        return config.entityModel.castOrOperationsEx<ExposifyEntityClass<ENTITY>>()
    }

    fun findHierarchyRoot(): RootDTO<*, *, *>{
        return when(this){
            is RootDTO-> this
            is DTOClass-> parentClass.findHierarchyRoot()
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
                findHierarchyRoot().let {hierarchyRoot->
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

    override val identity = asIdentifiableClass<RootDTO<DTO, DATA, ENTITY>, DTO>{dtoType.clazz}

    private var serviceContextParameter: ServiceContext<DTO, DATA, ENTITY>? = null
    val serviceContext: ServiceContext<DTO, DATA, ENTITY>
        get() = serviceContextParameter.getOrInitEx()



    fun initialization(serviceContext: ServiceContext<DTO, DATA, ENTITY>) {
        serviceContextParameter = serviceContext
        if (!initialized) setup()
    }
    fun reinitChild(){
        config.childClasses.forEach {
            if(!it.initialized){
                it.initialization()
            }
        }
    }

    override fun initializationComplete(){
        initialized = true
        notifier.trigger(Events.Initialized, this)
    }

    fun getServiceClass(): ServiceClass<DTO, DATA, ENTITY>{
       return serviceContext.serviceClass.getOrInitEx("ServiceClass not assigned for $completeName")
    }

    fun switchQuery(id: Long): SwitchQuery<DTO, DATA, ENTITY> {
        return SwitchQuery(id, this)
    }
    companion object: ValueBased{
        override val value: Int
            get() = 1
    }

}

abstract class DTOClass<DTO, DATA, ENTITY>(
    val parentClass: DTOBase<*, *, *>,
): DTOBase<DTO, DATA, ENTITY>(), ClassDTO, TasksManaged
        where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity {


    override val identity = asIdentifiableClass<DTOClass<DTO, DATA, ENTITY>, DTO>{dtoType.clazz}

    override fun initializationComplete(){
        initialized = true
        notifier.trigger(Events.Initialized, this)
    }

    fun initialization() {
        if (!initialized){ setup() }
    }

    companion object: ValueBased{
        override val value: Int
            get() = 2
    }

}

