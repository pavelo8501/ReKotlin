package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync
import po.misc.types.castOrThrow
import kotlin.reflect.KClass


abstract class RootDTO<DTO, DATA>()
    : DTOBase<DTO, DATA>(),  TasksManaged,  ClassDTO
        where DTO: ModelDTO, DATA: DataModel
{
    var serviceContextOwned: ServiceContext<DTO, DATA, ExposifyEntity>? = null

    @PublishedApi
    internal var initialConfig: DTOConfig<DTO, DataModel, ExposifyEntity>? = null
    override val config: DTOConfig<DTO, DataModel, ExposifyEntity>
        get() = initialConfig.getOrInitEx("RegistryItem uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)

    suspend fun initialization() {
        if (!initialized) setup()
    }

    fun setContextOwned(contextOwned: ServiceContext<DTO, DATA, ExposifyEntity>){
        serviceContextOwned = contextOwned
    }

    inline fun <reified COMMON,  reified DATA, reified ENTITY> configuration(
        entityModel: LongEntityClass<ENTITY>,
        noinline block: suspend DTOConfig<DTO, DATA, ENTITY>.() -> Unit
    ): Unit where COMMON : ModelDTO,  ENTITY : ExposifyEntity, DATA : DataModel = startTaskAsync("DTO Configuration") {

        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTO<DTO, DATA, ENTITY>>>(
            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")

        val newRegistryItem = DTORegistryItem(DATA::class, ENTITY::class, commonDTOClass)
        val newConfiguration = DTOConfig(newRegistryItem, entityModel, this)
        newConfiguration.block()
        initialConfig = newConfiguration.castOrInitEx<DTOConfig<DTO, DataModel, ExposifyEntity>>()
        initialized = true
    }.resultOrException()

    suspend fun <DATA: DataModel> runSequence(
        sequenceId: Int,
        handlerBlock: (suspend SequenceHandler<DTO, DATA>.()-> Unit)? = null):List<DATA> {

        return  withTransactionIfNone {
            val serviceContext = serviceContextOwned.getOrOperationsEx(
                "Unable to run sequence id: $sequenceId on DTOClass. DTOClass is not a hierarchy root",
                ExceptionCode.UNDEFINED
            )
            val handler = serviceContext.serviceClass.getSequenceHandler(sequenceId, this)
                .castOrOperationsEx<SequenceHandler<DTO, DATA>>()

            handlerBlock?.invoke(handler)
            val key =  handler.thisKey
            val result = serviceContext.serviceClass.runSequence(key).castOrOperationsEx<List<DATA>>()

            result
        }
    }


    suspend fun <DATA: DataModel> runSequence(
        sequenceID: SequenceID,
        handlerBlock: (suspend SequenceHandler<DTO, DATA>.()-> Unit)? = null) = runSequence(sequenceID.value, handlerBlock)
}

abstract class DTOClass<DTO>(
    val  parentClass: DTOBase<*, *>,
): DTOBase<DTO, DataModel>(),  TasksManaged,  ClassDTO where DTO: ModelDTO {

    @PublishedApi
    internal var initialConfig: DTOConfig<DTO, DataModel, ExposifyEntity>? = null

    override val config: DTOConfig<DTO, DataModel, ExposifyEntity>
        get() = initialConfig.getOrInitEx("RegistryItem uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)

    suspend fun initialization() {
        if (!initialized) setup()
    }

    inline fun <reified COMMON,  reified DATA, reified ENTITY> configuration(
        entityModel: LongEntityClass<ENTITY>,
        noinline block: suspend DTOConfig<DTO, DATA, ENTITY>.() -> Unit
    ): Unit where COMMON : ModelDTO,  ENTITY : ExposifyEntity, DATA : DataModel = startTaskAsync("DTO Configuration") {

        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTO<DTO, DATA, ENTITY>>>(
            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")

        val newRegistryItem = DTORegistryItem(DATA::class, ENTITY::class, commonDTOClass)
        val newConfiguration = DTOConfig(newRegistryItem, entityModel, this)
        newConfiguration.block()
        initialConfig =  newConfiguration.castOrInitEx<DTOConfig<DTO, DataModel, ExposifyEntity>>()
        initialized = true
    }.resultOrException()

}


sealed class DTOBase<DTO, DATA>(): TasksManaged,  ClassDTO
        where DTO: ModelDTO, DATA : DataModel{

    override var personalName: String = "DTO[Uninitialized]"
    override var initialized: Boolean = false

    abstract val config: DTOConfig<DTO, DataModel, ExposifyEntity>

    protected abstract suspend fun  setup()

    fun <ENTITY : ExposifyEntity> getEntityModel(): LongEntityClass<ENTITY> {
        return config.entityModel.castOrOperationsEx<LongEntityClass<ENTITY>>()
    }

    override fun findHierarchyRoot():ClassDTO?{
        when(this){
            is RootDTO<*, *>->{
                return this.castOrThrow<DTOClass<ModelDTO>, InitException>()
            }
            is DTOClass<*>->{
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
    ): CommonDTO<CHILD_DTO, DataModel, ExposifyEntity>?
    {
        val dtos : MutableList<CommonDTO<DTO, DataModel, ExposifyEntity>> = mutableListOf()

        if(this is RootDTO<*,*>){
            TODO("Not yet implemented")
           // dtos =  serviceContextOwned?.dtoMap?.values.toList()
        }
        return dtos.firstOrNull{ it.id == id }?.safeCast()
    }
}