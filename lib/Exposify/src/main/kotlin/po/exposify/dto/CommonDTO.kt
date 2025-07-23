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
import po.exposify.dto.models.CommonDTOType
import po.exposify.dto.models.DTOId
import po.exposify.extensions.castOrOperations
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.CallbackPayload
import po.misc.callbacks.builders.callbackBuilder
import po.misc.callbacks.builders.registerPayload
import po.misc.containers.BackingContainer
import po.misc.containers.LazyBackingContainer
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.exceptions.ExceptionPayload
import po.misc.types.TypeData
import java.util.UUID



abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): ModelDTO, TasksManaged where DTO:ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    enum class DataStatus {
        New,
        Dirty,
        UpToDate
    }

    enum class DTOEvents{
        OnDTOComplete
    }

    override val identity: CTXIdentity<out CTX> = asIdentity()

    private val exPayload: ExceptionPayload = ExceptionPayload(this)

    var onInitializationStatusChange: ((CommonDTO<DTO, DATA, ENTITY>) -> Unit)? = null

    override var status: DTOStatus = DTOStatus.Uninitialized
        internal set(value) {
                if (value != field) {
                    field = value
                    onInitializationStatusChange?.invoke(this)
                }
            }

    override var dataStatus:DataStatus= DataStatus.New
        internal set

    override val typeData: TypeData<DTO> get() = dtoClass.dtoType

    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY> get() =  dtoClass.config
    val commonType: LazyBackingContainer<CommonDTOType<DTO, DATA, ENTITY>> get() = dtoClass.commonDTOType

    override val dtoId : DTOId<DTO> = DTOId(UUID.randomUUID().hashCode().toLong())

    override val contextName: String
        get() = "CommonDTO[${sourceName}#${dtoId.id}]"

    var sourceName: String = typeData.simpleName

    override val tracker: DTOTracker<DTO, DATA, ENTITY> = DTOTracker(this)
    override val hub: BindingHub<DTO, DATA, ENTITY> =  BindingHub(this)

    val dataContainer : BackingContainer<DATA> = BackingContainer<DATA>(exPayload, dtoClass.dataType)
    val entityContainer : BackingContainer<ENTITY> = BackingContainer<ENTITY>(exPayload, dtoClass.entityType)

    private val executionContextMap: MutableMap<CommonDTOType<*, *, *>, DTOExecutionContext<*, *, *, DTO, DATA, ENTITY>> = mutableMapOf()

    internal val onDTOComplete = CallbackPayload.createPayload<DTOEvents, CommonDTO<DTO, DATA, ENTITY>>(DTOEvents.OnDTOComplete)
    internal val notifier: CallbackManager<DTOEvents> = callbackBuilder {
        registerPayload(onDTOComplete)
    }

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> registerExecutionContext(
        commonDTOType: CommonDTOType<F, FD, FE>,
        context: DTOExecutionContext<F, FD, FE, DTO, DATA, ENTITY>
    ){
        executionContextMap.put(commonDTOType,  context)
    }

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> getExecutionContext(
        foreignDTO: DTOClass<F, FD, FE>,
        warnIfEmpty: Boolean = false
    ): DTOExecutionContext<F, FD, FE, DTO, DATA, ENTITY>?{

       val found = foreignDTO.commonDTOType.getValue()?.let { executionContextMap[it] }
       return found?.castOrOperations<DTOExecutionContext<F, FD, FE, DTO, DATA, ENTITY>>(exPayload)?:run {
            if(warnIfEmpty && executionContextMap.isEmpty()){
                logHandler.dataProcessor.warn("executionContextMap is empty")
            }
           null
        }
    }

    val logger : LogNotifyHandler get() {
        tracker.logDebug("Accessing logger", this)
        return logHandler
    }

    override var cardinality: Cardinality = Cardinality.ONE_TO_MANY

    override val daoService: DAOService<DTO, DATA, ENTITY> get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, DATA, ENTITY> get() = dtoClassConfig.dtoFactory

    private var idBacking: Long = -1L
    override var id: Long = idBacking
        set(value){
            if(value != field && value > 0){
                field = value
            }
        }

    fun updateId(providedId: Long) {
        id = providedId
        if(dataContainer.isSourceAvailable){
            dataContainer.source.id = providedId
        }
    }

    internal fun provideEntity(entity: ENTITY, dtoStatus: DTOStatus): ENTITY {
        if(dtoStatus == DTOStatus.Complete){
            val entityId = entity.id.value
            updateId(entityId)
        }
        entityContainer.provideSource(entity, dtoClass.entityType)
        updateStatus(dtoStatus)
        return entity
    }
    internal fun provideData(data: DATA, dtoStatus: DTOStatus, initiator: CTX): DATA {
        if(data.id != 0L){
            id = data.id
        }
        dataContainer.provideSource(data, dtoClass.dataType)
        updateStatus(dtoStatus)
        return data
    }

    internal fun updateStatus(dtoStatus: DTOStatus){
        if(dtoStatus == status){
            logHandler.dataProcessor.warn("DTO{$this} new status ${dtoStatus.name} while existing status ${status.name}")
        }
        when(dtoStatus){
            DTOStatus.Complete->{
                if(id  < 1){
                    val dataModel = dataContainer.source
                    if(dataModel.id == 0L){
                        val entityId = entityContainer.source.id.value
                        dataModel.id = entityId
                        tracker.dtoIdUpdated(entityId)
                    }
                }
                onDTOComplete.triggerForAll(this)
                status = dtoStatus
            }
            else -> {
                status = dtoStatus
            }
        }
    }

    internal fun initialize(trackerConfig: TrackerConfig? = null): CommonDTO<DTO, DATA, ENTITY> {
        if (trackerConfig != null) {
            tracker.updateConfig(trackerConfig)
        }
        return this
    }
}