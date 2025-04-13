package po.exposify.classes

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.transactions.TransactionManager
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.classes.components.DTOConfig
import po.exposify.dto.components.RootRepository
import po.exposify.classes.interfaces.DTOInstance
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.safeCast
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.startTask
import po.lognotify.extensions.startTaskAsync
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

abstract class DTOClass<DTO>(): TasksManaged,  DTOInstance where DTO: ModelDTO{

    lateinit var registryItem : DTORegistryItem<DTO, DataModel, ExposifyEntityBase>

    override var personalName: String = "DTOClass[undefined]"
        protected set

    var initialized: Boolean = false

    private lateinit var configInstance: DTOConfig<DTO, DataModel, ExposifyEntityBase>
    internal val config: DTOConfig<DTO, DataModel, ExposifyEntityBase>
        get() = configInstance

    var serviceContextOwned: ServiceContext<DTO, DataModel>? = null
    var repository : RootRepository<DTO, DataModel, ExposifyEntityBase, DTO>? = null

   protected abstract fun setup()

  fun <DATA : DataModel, ENTITY: ExposifyEntityBase> applyConfig(initializedConfig : DTOConfig<DTO, DATA, ENTITY>) {
        initializedConfig.safeCast<DTOConfig<DTO, DataModel, ExposifyEntityBase>>()?.let {
            configInstance = it
        }?: throw InitException("Safe cast failed for DTOConfig2", ExceptionCode.CAST_FAILURE)

       val casted = initializedConfig.dtoRegItem.safeCast<DTORegistryItem<DTO, DataModel, ExposifyEntityBase>>()
           .getOrThrowDefault("Cast to DTORegistryItem<DTO, DataModel, ExposifyEntityBase> failed")
       registryItem = casted
       personalName = "DTOClass[${registryItem.commonDTOKClass.simpleName}]"
       initFactoryRoutines()
       initialized = true
   }

   inline fun <reified DATA, reified ENTITY> configuration(
       dtoClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
       entityModel: LongEntityClass<ENTITY>,
       block: DTOConfig<DTO, DATA, ENTITY>.() -> Unit
   ) where ENTITY: ExposifyEntityBase, DATA: DataModel {

       val newRegistryItem = DTORegistryItem<DTO, DATA, ENTITY>(dtoClass, DATA::class, ENTITY::class, this@DTOClass)
       val configuration = DTOConfig(newRegistryItem, entityModel, this)
       configuration.block()
       applyConfig(configuration)
    }

    fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>){
        cumulativeList.add(configInstance.entityModel.table)
        runBlocking {
            withRelationshipBinder() {
                this.childBindings.values.forEach {
                    it.childClass.getAssociatedTables(cumulativeList)
                }
            }
        }
    }

    fun <ENTITY: ExposifyEntityBase> getEntityModel(): LongEntityClass<ENTITY>{
        return config.entityModel.safeCast<LongEntityClass<ENTITY>>().getOrThrowDefault("Cast to LongEntityClass<ENTITY> failed")
    }

    fun initialization(onRequestFn: (() -> Unit)? = null) {
        if(!initialized){
            setup()
        }
    }

    fun <DATA: DataModel>asHierarchyRoot(serviceContext: ServiceContext<DTO, DATA>){
        if(serviceContextOwned == null){
            serviceContext.safeCast<ServiceContext<DTO, DataModel>>()?.let {
                serviceContextOwned = it
                repository = RootRepository<DTO, DataModel, ExposifyEntityBase, DTO>(this)
                initFactoryRoutines()
            }?: throw InitException("Cast for ServiceContext2 failed", ExceptionCode.CAST_FAILURE)
        }
    }

    fun initFactoryRoutines(): Unit = config.initFactoryRoutines()

    suspend fun withRelationshipBinder(
        block: suspend RelationshipBinder<DTO, DataModel, ExposifyEntityBase>.()-> Unit
    ): Unit = config.withRelationshipBinder(block)

    fun isTransactionReady(): Boolean {
        return TransactionManager.currentOrNull()?.connection?.isClosed?.not() == true
    }


    suspend fun <DATA: DataModel>  runSequence(
        sequenceId: Int,
        coroutineContext: CoroutineContext,
        handlerBlock: suspend SequenceHandler<DTO, DataModel>.()-> Unit):List<DATA>
        = startTask("Run Sequence", coroutineContext, personalName) {
         withTransactionIfNone {
             val serviceContext = serviceContextOwned.getOrThrowDefault("Unable to run sequence id: $sequenceId on DTOClass. DTOClass is not a hierarchy root")
             val handler = serviceContext.serviceClass().getSequenceHandler(sequenceId, this)
             handlerBlock.invoke(handler)
             val key =  handler.thisKey
             val result = serviceContext.serviceClass().runSequence(key).safeCast<List<DATA>>()
                 .getOrThrowDefault("Cast to List<DATA> failed")
             result
         }
    }.resultOrException()

    suspend fun <DATA: DataModel> runSequence(
        sequenceID: SequenceID,
        coroutineContext: CoroutineContext,
        handlerBlock: suspend SequenceHandler<DTO, DataModel>.()-> Unit):List<DATA>
        = runSequence(sequenceID.value, coroutineContext,  handlerBlock)
}