package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.DTOExecutionContext
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.components.tracker.models.TrackerConfig
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.enums.DTOStatus
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.models.CommonDTOType
import po.exposify.dto.models.DTOId
import po.exposify.exceptions.OperationsException
import po.lognotify.TasksManaged
import po.misc.containers.BackingContainer
import po.misc.containers.LazyBackingContainer
import po.misc.containers.ReactiveMap
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.functions.subscribers.TaggedLambdaRegistry
import java.util.UUID


sealed class CommonDTOBase<DTO, D, E>(
    val dtoClass: DTOBase<DTO, D, E>
): ModelDTO, TasksManaged where DTO:ModelDTO, D: DataModel, E: LongEntity {


    enum class DTOEvents{ Initialized, StatusUpdated }

    var dataStatus : DataStatus = DataStatus.New
        private set

    var dtoStatus: DTOStatus = DTOStatus.Uninitialized
        private set

    override var cardinality: Cardinality = Cardinality.ONE_TO_MANY

    protected val dtoClassConfig: DTOConfig<DTO, D, E> get() =  dtoClass.dtoConfiguration

    override val commonType: CommonDTOType<DTO, D, E> by lazy { dtoClass.commonDTOType.copy() }

    override val daoService: DAOService<DTO, D, E> get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, D, E> get() = dtoClassConfig.dtoFactory

    val onDTOComplete: TaggedLambdaRegistry<DTOEvents, CommonDTO<DTO, D, E>> = TaggedLambdaRegistry(DTOEvents::class.java)
    val onStatusUpdated: TaggedLambdaRegistry<DTOEvents, CommonDTO<DTO, D, E>> = TaggedLambdaRegistry(DTOEvents::class.java)

    val executionContextMap: ReactiveMap<CommonDTOType<*, *, *>, DTOExecutionContext<*, *, *, DTO, D, E>> = ReactiveMap()
    val executionContextsCount: Int get() = executionContextMap.size

    init {
        executionContextMap.injectFallback{ OperationsException(it) }
    }

    protected fun changeDataStatus(status: DataStatus){
        dataStatus = status
    }

    protected fun changeDTOStatus(status: DTOStatus){
        if (dtoStatus != status) {
            when(this){
                is CommonDTO -> {
                    onStatusUpdated.trigger(DTOEvents.StatusUpdated, this)
                }
            }
        }
        dtoStatus = status
    }
}


abstract class CommonDTO<DTO, DATA, ENTITY>(
   dtoClass: DTOBase<DTO, DATA, ENTITY>
): CommonDTOBase<DTO, DATA, ENTITY>(dtoClass) where DTO:ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    override val identity: CTXIdentity<CommonDTO<DTO, DATA, ENTITY>> = asIdentity()

    override val dtoId : DTOId<DTO> = DTOId(UUID.randomUUID().hashCode().toLong())

    override val tracker: DTOTracker<DTO, DATA, ENTITY> = DTOTracker(this)
    override val hub: BindingHub<DTO, DATA, ENTITY> =  BindingHub(this)

    val dataContainer : BackingContainer<DATA> = BackingContainer(commonType.dataType)

    val entityContainer : BackingContainer<ENTITY> = BackingContainer(commonType.entityType)
    val parentDTO: LazyBackingContainer<CommonDTO<*, *, *>> = LazyBackingContainer()

    init {
        identity.setNamePattern { "CommonDTO[${commonType.dataType.simpleName}#${dtoId.id}]" }
    }

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> registerExecutionContext(
        commonType: CommonDTOType<F, FD, FE>,
        context: DTOExecutionContext<F, FD, FE, DTO, DATA, ENTITY>
    ){
        println("registerExecutionContext on $this  commonType=${commonType}  context.class = ${context.dtoClass} context.hostingDTO= ${context.hostingDTO}")
        executionContextMap.put(commonType,  context)
    }

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> registerParentDTO(dto: CommonDTO<F, FD, FE>){
        parentDTO.provideValue(dto)
    }
    private var idBacking: Long = -1L
    override var id: Long = idBacking
        set(value){
            if(value != field && value > 0){
                field = value
            }
        }

    fun updateId(providedId: Long) {
        id = providedId
        if(dataContainer.isValueAvailable){
            dataContainer.getValue(this).id = providedId
        }
    }

    internal fun provideEntity(entity: ENTITY, dtoStatus: DTOStatus): ENTITY {
        if(dtoStatus == DTOStatus.Complete){
            val entityId = entity.id.value
            updateId(entityId)
        }
        entityContainer.provideValue(entity)
        updateStatus(dtoStatus)
        return entity
    }
    internal fun provideData(data: DATA, dtoStatus: DTOStatus, initiator: CTX): DATA {
        if(data.id != 0L){
            id = data.id
        }
        dataContainer.provideValue(data)
        updateStatus(dtoStatus)
        return data
    }

    internal fun updateStatus(status: DTOStatus){
        if(dtoStatus == status){
            notify("DTO{$this} new status ${dtoStatus.name} while existing status ${status.name}", SeverityLevel.WARNING)
        }
        when(dtoStatus){
            DTOStatus.Complete->{
                if(id  < 1){
                    val dataModel = dataContainer.getValue(this)
                    if(dataModel.id == 0L){
                        val entityId = entityContainer.getValue(this).id.value
                        dataModel.id = entityId
                        tracker.dtoIdUpdated(entityId)
                    }
                }
                onDTOComplete.trigger(DTOEvents.Initialized, this)
                changeDTOStatus(status)
            }
            else -> {
                changeDTOStatus(status)
            }
        }
    }

    internal fun initialize(trackerConfig: TrackerConfig? = null): CommonDTO<DTO, DATA, ENTITY> {
        if (trackerConfig != null) {
            tracker.updateConfig(trackerConfig)
        }
        return this
    }

    override fun toString(): String = identity.identifiedByName

}