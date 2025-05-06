package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityClass
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.SequenceHandlerBase
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.models.RootSequencePack
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync
import po.misc.collections.Identifiable
import po.misc.collections.generateKey
import po.misc.types.castOrThrow
import kotlin.reflect.KClass


abstract class RootDTO<DTO, DATA>()
    : DTOBase<DTO, DATA>(),  TasksManaged,  ClassDTO
        where DTO: ModelDTO, DATA: DataModel
{
    var serviceContextOwned: ServiceContext<DTO, DATA, LongEntity>? = null

    @PublishedApi
    internal var initialConfig: DTOConfig<DTO, DataModel, LongEntity>? = null
    override val config: DTOConfig<DTO, DataModel, LongEntity>
        get() = initialConfig.getOrInitEx("RegistryItem uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)

    override val qualifiedName: String get() = "DTOClass[${config.registry.dtoRootName}]"

    suspend fun initialization() {
        if (!initialized) setup()
    }

    fun setContextOwned(contextOwned: ServiceContext<DTO, DATA, LongEntity>){
        serviceContextOwned = contextOwned
    }

    fun getServiceClass(): ServiceClass<DTO, DATA, LongEntity>{
       return   serviceContextOwned?.serviceClass.getOrInitEx("ServiceClass not assigned for ${qualifiedName}")
    }

    inline fun <reified COMMON,  reified DATA, reified ENTITY> configuration(
        entityModel: ExposifyEntityClass<ENTITY>,
        noinline block: suspend DTOConfig<DTO, DATA, ENTITY>.() -> Unit
    ): Unit where COMMON : ModelDTO,  ENTITY : LongEntity, DATA : DataModel = startTaskAsync("DTO Configuration") {

        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTO<DTO, DATA, ENTITY>>>(
            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")

        val newRegistryItem = DTORegistryItem(DATA::class, ENTITY::class, commonDTOClass)
        val newConfiguration = DTOConfig(newRegistryItem, entityModel, this)
        newConfiguration.block()
        initialConfig = newConfiguration.castOrInitEx<DTOConfig<DTO, DataModel, LongEntity>>()
        initialized = true
    }.resultOrException()


    suspend fun runSequence(
        sequenceID : SequenceID,
        handlerBlock : suspend RootSequenceHandler<DTO, DATA>.()-> Unit
    ): List<DATA>{
        return withTransactionIfNone {
            val serviceClass = getServiceClass()
            val pack = serviceClass.getSequencePack(generateKey(sequenceID))
                .castOrThrow<RootSequencePack<DTO, DATA>, OperationsException>()
            val emitter = serviceClass.requestEmitter()
            emitter.dispatch<DTO, DATA, List<DATA>>(pack, handlerBlock)
        }
    }

//   private  suspend fun <R> runSequencePrivate(
//        sequenceID: SequenceID,
//        handlerBlock: (suspend SequenceHandler<DATA>.()-> R)? = null): R {
//
//        return  withTransactionIfNone {
//            val serviceContext = serviceContextOwned.getOrOperationsEx(
//                "Unable to run sequence id: ${sequenceID.name} on DTOClass. DTOClass is not a hierarchy root",
//                ExceptionCode.UNDEFINED
//            )
//            val pack = serviceContext.serviceClass.getSequencePack(this.generateKey(sequenceID))
//            val emitter = serviceContext.serviceClass.requestEmitter()
//            handlerBlock?.invoke()
//            emitter.dispatch(this,  pack)
//
//            handlerBlock?.invoke(handler)
//            val key =  handler.thisKey
//            serviceContext.serviceClass.runSequence(key)
//        }
//    }


//    suspend fun <R> runSequence(
//        sequenceID: SequenceID,
//        handlerBlock: (suspend SequenceHandler<DATA>.()->  R)? = null):R
//            = runSequence(sequenceID, handlerBlock)
}

abstract class DTOClass<DTO, DATA>(
    val  parentClass: DTOBase<*, *>,
): DTOBase<DTO, DATA>(), ClassDTO, TasksManaged where DTO: ModelDTO, DATA : DataModel {

    @PublishedApi
    internal var initialConfig: DTOConfig<DTO, DataModel, LongEntity>? = null

    override val config: DTOConfig<DTO, DataModel, LongEntity>
        get() = initialConfig.getOrInitEx("RegistryItem uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)

    override val qualifiedName: String get() = "DTOClass[${config.registry.dtoClassName}]"

    suspend fun initialization() {
        if (!initialized) setup()
    }

    inline fun <reified COMMON,  reified DATA, reified ENTITY> configuration(
        entityModel: ExposifyEntityClass<ENTITY>,
        noinline block: suspend DTOConfig<DTO, DATA, ENTITY>.() -> Unit
    ): Unit where COMMON : ModelDTO,  ENTITY : LongEntity, DATA : DataModel = startTaskAsync("DTO Configuration") {

        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTO<DTO, DATA, ENTITY>>>(
            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")

        val newRegistryItem = DTORegistryItem(DATA::class, ENTITY::class, commonDTOClass)
        val newConfiguration = DTOConfig(newRegistryItem, entityModel, this)
        newConfiguration.block()
        initialConfig =  newConfiguration.castOrInitEx<DTOConfig<DTO, DataModel, LongEntity>>()
        initialized = true
    }.resultOrException()

}


sealed class DTOBase<DTO, DATA>(): TasksManaged,  ClassDTO, Identifiable
        where DTO: ModelDTO, DATA : DataModel{

    abstract val config: DTOConfig<DTO, DataModel, LongEntity>
    override var personalName: String = "DTO[Uninitialized]"
    override var initialized: Boolean = false
    abstract override val qualifiedName: String

    protected abstract suspend fun  setup()

    fun <ENTITY : LongEntity> getEntityModel(): ExposifyEntityClass<ENTITY> {
        return config.entityModel.castOrOperationsEx<ExposifyEntityClass<ENTITY>>()
    }

    override fun findHierarchyRoot():ClassDTO?{
        when(this){
            is RootDTO<*, *>->{
                return this.castOrThrow<DTOClass<ModelDTO, DataModel>, InitException>()
            }
            is DTOClass<*, *>->{
                parentClass.getOrOperationsEx("parentDtoClass not initialized").findHierarchyRoot()
            }
        }
        return null
    }

    override fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(config.entityModel.table)
        config.relationBinder.getChildClassList().forEach {
            it.getAssociatedTables(cumulativeList)
        }
    }


    internal fun <CHILD_DTO: ModelDTO>  lookupDTO(
        id: Long,
        childDtoClass: DTOBase<CHILD_DTO, *,>
    ): CommonDTO<CHILD_DTO, DataModel, LongEntity>?
    {
        val dtos : MutableList<CommonDTO<DTO, DataModel, LongEntity>> = mutableListOf()

        if(this is RootDTO<*,*>){
            TODO("Not yet implemented")
           // dtos =  serviceContextOwned?.dtoMap?.values.toList()
        }
        return dtos.firstOrNull{ it.id == id }?.safeCast()
    }
}