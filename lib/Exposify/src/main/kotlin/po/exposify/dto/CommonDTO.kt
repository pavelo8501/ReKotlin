package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.transaction.withTransactionIfNone
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.executioncontext.DTOExecutionContext
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.bindings.RelationDelegateKey
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.components.tracker.models.TrackerConfig
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.enums.DTOStatus
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.dto.models.DTOId
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.lognotify.TasksManaged
import po.misc.containers.backing.BackingContainer
import po.misc.containers.ReactiveMap
import po.misc.containers.backing.backingContainerOf
import po.misc.containers.lazy.LazyContainer
import po.misc.containers.lazy.lazyContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.throwableToText
import po.misc.functions.hooks.Change
import po.misc.functions.registries.TaggedRegistry
import po.misc.functions.registries.builders.taggedRegistryOf
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.safeCast
import java.util.UUID
import kotlin.reflect.KClass

sealed class CommonDTOBase<DTO, D, E>(
    val dtoClass: DTOBase<DTO, D, E>,
) : ModelDTO,
    TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {
    enum class DTOEvents {
        Initialized,
        StatusUpdated,
        IdResolved,
    }

    var dataStatus: DataStatus = DataStatus.New
        private set

    var dtoStatus: DTOStatus = DTOStatus.Uninitialized
        protected set

    override var cardinality: Cardinality = Cardinality.ONE_TO_MANY
    override val dtoId: DTOId<DTO> = DTOId(UUID.randomUUID().hashCode().toLong())

    protected val dtoClassConfig: DTOConfig<DTO, D, E> get() = dtoClass.dtoConfiguration

    override val commonType: CommonDTOType<DTO, D, E> by lazy { dtoClass.commonDTOType.copy() }

    override val daoService: DAOService<DTO, D, E> get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, D, E> get() = dtoClassConfig.dtoFactory


    val onDTOComplete: TaggedRegistry<DTOEvents, CommonDTO<DTO, D, E>> by lazy { taggedRegistryOf(DTOEvents.Initialized) }
    val onStatusUpdated: TaggedRegistry<DTOEvents, CommonDTO<DTO, D, E>> by lazy{ taggedRegistryOf(DTOEvents.StatusUpdated) }
    val onIdResolved: TaggedRegistry<DTOEvents, CommonDTO<DTO, D, E>> by lazy {
        taggedRegistryOf(DTOEvents.IdResolved){ warnNoSubscriber(false) }
    }

    val dataContainer: LazyContainer<D> = lazyContainerOf(commonType.dataType)
    val entityContainer: BackingContainer<E> = backingContainerOf(commonType.entityType.typeToken)
    
    var idBacking: Long = -1L
    override var id: Long
        get() = idBacking
        set(value) {
            if (value != idBacking) {
                idBacking = value
            } else {
                warning("Setting same id $value")
            }
        }


    protected fun calculateStatus(): DTOStatus {
        if(dataStatus == DataStatus.PreflightCheckMock && dataContainer.valueAvailable){
            return DTOStatus.Complete
        }
        var resultingStatus: DTOStatus = DTOStatus.Uninitialized
        if (dataContainer.valueAvailable && entityContainer.valueAvailable) {
            resultingStatus = DTOStatus.Complete
        }
        if (dataContainer.valueAvailable && !entityContainer.valueAvailable) {
            resultingStatus = DTOStatus.PartialWithData
        }
        if (!dataContainer.valueAvailable && entityContainer.valueAvailable) {
            resultingStatus = DTOStatus.PartialWithEntity
        }
        return resultingStatus
    }

    protected fun updateDtoId(newID: Long) {
        if (newID > 0) {
            idBacking = newID
            dataContainer.value?.let { dataModel ->
                dataModel.id = newID
            } ?: run {
                notify("Unable to update id on dataModel. Container empty", SeverityLevel.WARNING)
            }
            identity.setNamePattern {
                "${commonType.dtoType.typeName}#$idBacking"
            }

            if(this is CommonDTO){
              //  onIdResolved.trigger(this)
            }
        } else {
            notify("Trying to update id with value $newID ", SeverityLevel.WARNING)
        }
    }

    fun updateDataStatus(status: DataStatus):CommonDTO<DTO, D, E>{
        dataStatus = status
        if(status == DataStatus.PreflightCheckMock){
            calculateStatus()
            updateDtoId(1)
        }
        return this as CommonDTO<DTO, D, E>
    }
}

abstract class CommonDTO<DTO, D, E>(
    dtoClass: DTOBase<DTO, D, E>,
) : CommonDTOBase<DTO, D, E>(dtoClass) where DTO : ModelDTO, D : DataModel, E : LongEntity {
    override val identity: CTXIdentity<CommonDTO<DTO, D, E>> = asIdentity()

    override val tracker: DTOTracker<DTO, D, E> = DTOTracker(this)
    override val bindingHub: BindingHub<DTO, D, E> = BindingHub(this)

    val parentDTOContainer: LazyContainer<CommonDTO<*, *, *>> = lazyContainerOf()

    val executionContextMap: ReactiveMap<DTOClass<*, *, *>, DTOExecutionContext<DTO, D, E, *, *, *>> = ReactiveMap()
    val executionContextsCount: Int get() = executionContextMap.size
    val relations: Map<RelationDelegateKey<*, *, *>, RelationDelegate<DTO, D, E, *, *, *, *>> get() = bindingHub.relationDelegateMap

    
    init {
        entityContainer.onValueChanged(::entityChanged)
        dataContainer.valueProvided(this, ::dataModelChanged)
        executionContextMap.onErrorHook.subscribe { warning(it.toString()) }
        executionContextMap.injectFallback { operationsException("Requested execution context not created/saved", ExceptionCode.ABNORMAL_STATE) }
    }

    private fun entityChanged(change: Change<E?, E>) {
        try {
            val entityId = change.newValue.id.value
            updateDtoId(entityId)
        } catch (th: Throwable) {
            if (th !is IllegalStateException) {
                println(th.throwableToText())
            }
        }
        updateStatus()
    }

    private fun dataModelChanged(value: D) {
        updateStatus()
    }

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> createExecutionContext(
        childClass: DTOClass<F, FD, FE>,
    ):DTOExecutionContext<DTO, D, E, F, FD, FE>{
        val newContext : DTOExecutionContext<DTO, D, E, F, FD, FE> = DTOExecutionContext(this, childClass)
        childClass.commonDTOType
        executionContextMap[childClass] = newContext
        return newContext
    }

    fun  updateStatus() {
        val newStatus = calculateStatus()
        if (dtoStatus != newStatus) {
            dtoStatus = newStatus
            onStatusUpdated.trigger(this)
            if (newStatus == DTOStatus.Complete) {
                onDTOComplete.trigger(this)
            }
        } else {
            if (newStatus != DTOStatus.Complete) {
                notify("DTO{$this} new status ${newStatus.name} while existing status ${dtoStatus.name}", SeverityLevel.WARNING)
            }
        }
    }

    internal suspend fun saveDTO(rootClass: RootDTO<DTO, D, E>){
        rootClass.executionContext.restoreDTO(dataContainer.getValue(this))
    }

    override fun flush(){
        withTransactionIfNone(dtoClass.debugger, false){
            bindingHub.responsiveDelegateMap.values.forEach {
                it.buffer.flush()
            }
        }
        updateDataStatus(DataStatus.UpToDate)
    }
   override fun flush(dataModel: DataModel): Boolean{
       val dataClass = dtoClass.commonDTOType.dataType.kClass
      return  dataModel.safeCast<D>(dataClass)?.let {
            bindingHub.updateByData(it)
            true
        }?:run {
           val providedClass = dataModel::class as KClass<*>
           val errMsg = "Unable to update persistence layer by data model provided. " +
                    "Expecting ${dataClass.simpleOrAnon}, got ${providedClass.simpleOrAnon}"
            warning(errMsg)
            false
        }
    }

    fun <F : ModelDTO, FD : DataModel, FE : LongEntity> hasExecutionContext(commonType: DTOClass<F, FD, FE>): Boolean =
        executionContextMap.containsKey(commonType)

    internal fun initialize(trackerConfig: TrackerConfig<*>? = null): CommonDTO<DTO, D, E> {
        if (trackerConfig != null) {
          //  tracker.updateConfig(trackerConfig)
        }
        return this
    }

    override fun toString(): String = "${commonType.dtoType.typeName} ${bindingHub.dtoStateDump} ${bindingHub.dtoRelationDump}"
}
