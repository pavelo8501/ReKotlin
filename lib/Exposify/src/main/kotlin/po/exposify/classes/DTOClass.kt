package po.exposify.classes

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.transactions.TransactionManager
import po.exposify.classes.components.DTOConfig
import po.exposify.classes.interfaces.ClassDTO
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistry
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


abstract class DTOClass<DTO>(): TasksManaged,  ClassDTO where DTO: ModelDTO {

    var _registryItem: DTORegistry<DTO, DataModel, ExposifyEntity>? = null
    val registryItem: DTORegistry<DTO, DataModel, ExposifyEntity>
        get() {return  _registryItem.getOrInitEx("RegistryItem uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)}

    override val personalName: String get() {return _registryItem?.dtoClassName?:"Undefined"}
    var initialized: Boolean = false

    var hierarchyRoot : Boolean = false

    @PublishedApi
    internal var _config: DTOConfig<DTO, DataModel, ExposifyEntity>? = null
    internal val config: DTOConfig<DTO, DataModel, ExposifyEntity>
        get() = _config.getOrInitEx("RegistryItem uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)

    var serviceContextOwned: ServiceContext<DTO, DataModel>? = null
    //var repository: RootRepository<DTO, DataModel, ExposifyEntity, DTO, DataModel, ExposifyEntity>? = null

     protected abstract suspend fun  setup()

    inline fun <reified COMMON,  reified DATA, reified ENTITY> configuration(
        entityModel: LongEntityClass<ENTITY>,
        noinline block: suspend DTOConfig<DTO, DATA, ENTITY>.() -> Unit
    ): Unit where COMMON : ModelDTO,  ENTITY : ExposifyEntity, DATA : DataModel = startTaskAsync("DTO Configuration") {

        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTO<DTO, DATA, ENTITY>>>(
            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")

         val newRegistryItem = DTORegistryItem(this, DATA::class, ENTITY::class, commonDTOClass)
         val newConfiguration = DTOConfig(newRegistryItem, entityModel, this)
         _registryItem  = newRegistryItem.castOrInitEx<DTORegistry<DTO, DataModel, ExposifyEntity>>()
         _config =  newConfiguration.castOrInitEx<DTOConfig<DTO, DataModel, ExposifyEntity>>()
         newConfiguration.block()
         initialized = true
    }.resultOrException()

    fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(config.entityModel.table)

//            withRelationshipBinder() {
//                this.childBindings.values.forEach {
//                    it.childClass.getAssociatedTables(cumulativeList)
//                }
//            }

    }

    fun <ENTITY : ExposifyEntity> getEntityModel(): LongEntityClass<ENTITY> {
        return config.entityModel.castOrOperationsEx<LongEntityClass<ENTITY>>()
    }

    suspend fun initialization() {
        if (!initialized) {
            setup()

        }
    }

    var parentDtoClass: DTOClass<ModelDTO>? = null
        private set
    internal fun setParentDTO(dtoClass: DTOClass<ModelDTO>){
        parentDtoClass = dtoClass
    }
    internal fun findHierarchyRoot():DTOClass<ModelDTO>?{
        if(hierarchyRoot){
            return this.castOrThrow<DTOClass<ModelDTO>, InitException>()
        }else{
            parentDtoClass.getOrOperationsEx("parentDtoClass not initialized").findHierarchyRoot()
        }
        return null
    }

    internal fun <CHILD_DTO: ModelDTO>  lookupDTO(
        id: Long,
        childDtoClass: DTOClass<CHILD_DTO>
    ): CommonDTO<CHILD_DTO, DataModel, ExposifyEntity>?
    {
      val dtos =  serviceContextOwned?.dtoMap?.values
        return dtos?.firstOrNull{ it.id == id }?.safeCast()
    }

    fun <DATA : DataModel> asHierarchyRoot(serviceContext: ServiceContext<DTO, DATA>) {
        if (serviceContextOwned == null) {
            serviceContext.safeCast<ServiceContext<DTO, DataModel>>()?.let {
                serviceContextOwned = it
//                repository = RootRepository(this)
                hierarchyRoot = true
            } ?: throw InitException("Cast for ServiceContext2 failed", ExceptionCode.CAST_FAILURE)
        }
    }


    fun isTransactionReady(): Boolean {
        return TransactionManager.currentOrNull()?.connection?.isClosed?.not() == true
    }

    suspend fun <DATA: DataModel> runSequence(
        sequenceId: Int,
        handlerBlock: (suspend SequenceHandler<DTO, DATA>.()-> Unit)? = null):List<DATA> {

     return  withTransactionIfNone {
            val serviceContext = serviceContextOwned.getOrOperationsEx(
                "Unable to run sequence id: $sequenceId on DTOClass. DTOClass is not a hierarchy root",
                ExceptionCode.UNDEFINED
            )
            val handler = serviceContext.serviceClass().getSequenceHandler(sequenceId, this)
                .castOrOperationsEx<SequenceHandler<DTO, DATA>>()

            handlerBlock?.invoke(handler)
            val key =  handler.thisKey
            val result = serviceContext.serviceClass().runSequence(key).castOrOperationsEx<List<DATA>>()

            result
        }
    }
    suspend fun <DATA: DataModel> runSequence(
        sequenceID: SequenceID,
        handlerBlock: (suspend SequenceHandler<DTO, DATA>.()-> Unit)? = null) = runSequence(sequenceID.value, handlerBlock)
}
