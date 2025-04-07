package po.exposify.classes

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.binders.relationship.RelationshipBinder2
import po.exposify.classes.components.CallbackEmitter2
import po.exposify.classes.components.DAOService2
import po.exposify.classes.components.DTOConfig2
import po.exposify.classes.components.DTOFactory
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
import po.exposify.scope.sequence.models.SequencePack2
import po.exposify.scope.service.ServiceContext
import po.managedtask.interfaces.TasksManaged
import kotlin.reflect.KClass

abstract class DTOClass<DTO>(): TasksManaged,  DTOInstance where DTO: ModelDTO{

    lateinit var registryItem : DTORegistryItem<DTO, DataModel, ExposifyEntityBase>

    var name: String = "DTOClass"
    override var personalName: String = "[$name|"
        protected set

    internal val emitter = CallbackEmitter2<DTO>()
    var initialized: Boolean = false

    private lateinit var configInstance: DTOConfig2<DTO, DataModel, ExposifyEntityBase>
    internal val config: DTOConfig2<DTO, DataModel, ExposifyEntityBase>
        get() = configInstance

    var serviceContextOwned: ServiceContext<DTO, DataModel>? = null
    var repository : RootRepository<DTO, DataModel, ExposifyEntityBase, DTO>? = null

   init {
       val stop = 10
   }

   protected abstract fun setup()

   internal fun <DATA : DataModel, ENTITY: ExposifyEntityBase> applyConfig(initializedConfig : DTOConfig2<DTO, DATA, ENTITY>) {
        initializedConfig.safeCast<DTOConfig2<DTO, DataModel, ExposifyEntityBase>>()?.let {
            configInstance = it
        }?: throw InitException("Safe cast failed for DTOConfig2", ExceptionCode.CAST_FAILURE)

       initializedConfig.dtoRegItem.safeCast<DTORegistryItem<DTO, DataModel, ExposifyEntityBase>>()?.let {
           registryItem = it
       }?: throw InitException("Safe cast failed for DTORegistryItem", ExceptionCode.CAST_FAILURE)
       personalName = "[$name|${this::class.simpleName.toString()}]"
       initFactoryRoutines()
       initialized = true
   }

   internal inline fun <reified DATA, reified ENTITY> configuration(
       dtoClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
       entityModel: LongEntityClass<ENTITY>,
       block: DTOConfig2<DTO, DATA, ENTITY>.() -> Unit
   ) where ENTITY: ExposifyEntityBase, DATA: DataModel {

       val newRegistryItem = DTORegistryItem<DTO, DATA, ENTITY>(dtoClass, DATA::class, ENTITY::class, this@DTOClass)
       val configuration = DTOConfig2(newRegistryItem, entityModel, this)
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

    fun initialization(onRequestFn: ((CallbackEmitter2<DTO>) -> Unit)? = null) {
        runCatching {
            if(!initialized){
                setup()
                onRequestFn?.invoke(emitter)
            }
        }.onFailure {
            println(it.message)
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
    internal suspend fun withFactory(block: suspend (DTOFactory<DTO, DataModel, ExposifyEntityBase>)-> Unit): Unit{
        return config.withFactory(block)
    }
    suspend fun withDaoService(block: suspend (DAOService2<DTO, DataModel, ExposifyEntityBase>)-> Unit): Unit = config.withDaoService(block)
    suspend fun withRelationshipBinder(block: suspend RelationshipBinder2<DTO, DataModel, ExposifyEntityBase>.()-> Unit): Unit = config.withRelationshipBinder(block)

    suspend fun triggerSequence(
        sequence: SequencePack2<DTO>
    ): Deferred<List<DataModel>> = emitter.launchSequence(sequence)

}