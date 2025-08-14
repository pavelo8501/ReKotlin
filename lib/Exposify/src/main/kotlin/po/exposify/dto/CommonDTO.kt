package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.DTOExecutionContext
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.BindingHub
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
import po.misc.containers.BackingContainer
import po.misc.containers.LazyContainer
import po.misc.containers.ReactiveMap
import po.misc.containers.backingContainerOf
import po.misc.containers.lazyContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.throwableToText
import po.misc.functions.hooks.Change
import po.misc.functions.registries.TaggedRegistry
import po.misc.functions.registries.builders.taggedRegistryOf
import java.util.UUID

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

    val dataContainer: LazyContainer<D> =  lazyContainerOf(commonType.dataType)
    val entityContainer: BackingContainer<E> = backingContainerOf(commonType.entityType)

    val executionContextMap: ReactiveMap<CommonDTOType<*, *, *>, DTOExecutionContext<*, *, *, DTO, D, *>> = ReactiveMap()
    val executionContextsCount: Int get() = executionContextMap.size

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

        if(dataStatus == DataStatus.PreflightCheckMock && dataContainer.isValueAvailable){
            return DTOStatus.Complete
        }

        var resultingStatus: DTOStatus = DTOStatus.Uninitialized
        if (dataContainer.isValueAvailable && entityContainer.isValueAvailable) {
            resultingStatus = DTOStatus.Complete
        }
        if (dataContainer.isValueAvailable && !entityContainer.isValueAvailable) {
            resultingStatus = DTOStatus.PartialWithData
        }
        if (!dataContainer.isValueAvailable && entityContainer.isValueAvailable) {
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
                onIdResolved.trigger(this)
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

    init {
        executionContextMap.onErrorHook.subscribe {
            warning(it.toString())
        }
        executionContextMap.injectFallback { operationsException("Requested execution context not created/saved", ExceptionCode.ABNORMAL_STATE) }
    }
}

abstract class CommonDTO<DTO, DATA, ENTITY>(
    dtoClass: DTOBase<DTO, DATA, ENTITY>,
) : CommonDTOBase<DTO, DATA, ENTITY>(dtoClass) where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity {
    override val identity: CTXIdentity<CommonDTO<DTO, DATA, ENTITY>> = asIdentity()

    override val tracker: DTOTracker<DTO, DATA, ENTITY> = DTOTracker(this)
    override val bindingHub: BindingHub<DTO, DATA, ENTITY> = BindingHub(this)



    val parentDTO: LazyContainer<CommonDTO<*, *, *>> = lazyContainerOf()

    init {
        entityContainer.onValueSet(::entityChanged)
        dataContainer.onValueSet(::dataModelChanged)
    }


    private fun entityChanged(change: Change<ENTITY?, ENTITY>) {
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

    private fun dataModelChanged(change: Change<DATA?, DATA>) {
        updateStatus()
    }

    fun updateStatus() {
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

    fun <F : ModelDTO, FD : DataModel, FE : LongEntity> registerExecutionContext(
        commonType: CommonDTOType<F, FD, FE>,
        context: DTOExecutionContext<F, FD, FE, DTO, DATA, ENTITY>,
    ) {
        executionContextMap[commonType] = context
    }

    /**
     * Child CommonDTO registering parent DTO on him self
     */
    fun <F : ModelDTO, FD : DataModel, FE : LongEntity> registerParentDTO(parentCommonDTO: CommonDTO<F, FD, FE>) {
        parentDTO.provideValue(parentCommonDTO)
        bindingHub.resolveParent(parentCommonDTO)
    }

    fun <F : ModelDTO, FD : DataModel, FE : LongEntity> hasExecutionContext(commonType: CommonDTOType<F, FD, FE>): Boolean =
        executionContextMap.containsKey(commonType)

    internal fun initialize(trackerConfig: TrackerConfig<*>? = null): CommonDTO<DTO, DATA, ENTITY> {
        if (trackerConfig != null) {
            tracker.updateConfig(trackerConfig)
        }
        return this
    }

    override fun toString(): String = "${commonType.dtoType.typeName} ${bindingHub.dtoStateDump}"
}
