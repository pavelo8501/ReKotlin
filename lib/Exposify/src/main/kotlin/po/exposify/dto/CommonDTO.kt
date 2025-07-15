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
import po.exposify.dto.models.DTOId
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.misc.interfaces.CtxId
import po.misc.interfaces.TypedContext
import po.misc.types.TypeData
import po.misc.types.TypeRecord
import java.util.UUID



abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): ModelDTO, TypedContext<DTO>, TasksManaged where DTO:ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    enum class DataStatus {
        New,
        Dirty,
        UpToDate
    }

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

    val commonType: TypeData<CommonDTO<DTO, DATA, ENTITY>> by lazy { dtoClass.commonType.toTypeData() }
    override val typeData: TypeData<DTO> get() = dtoClass.dtoType
    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY> get() =  dtoClass.config

    override val dtoId : DTOId<DTO> = DTOId(UUID.randomUUID().hashCode().toLong())
    override val contextName: String
        get() = "CommonDTO[${sourceName}#${dtoId.id}]"
    override var sourceName: String = typeData.simpleName

    override val tracker: DTOTracker<DTO, DATA, ENTITY> = DTOTracker(this)
    override val hub: BindingHub<DTO, DATA, ENTITY> =  BindingHub(this)
    internal val executionContext: DTOExecutionContext<DTO, DATA, ENTITY, DTO, DATA, ENTITY> = DTOExecutionContext(this, dtoClass)

    internal val dataModel:DATA get() {
        return executionContext.getDataModel(this)
    }

    val entityType: TypeRecord<ENTITY> get() = dtoClass.entityType
    val dataType:  TypeRecord<DATA> get() = dtoClass.dataType

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

//    internal fun toDTOResult():DTOResult<DTO>{
//        return DTOResult(this as ModelDTO<DTO>)
//    }

    fun updateId(providedId: Long) {
        id = providedId

        executionContext.getDataModelOrNull()?.let {
            it.id = providedId
            tracker.dtoIdUpdated(providedId)
        }
    }

    internal fun provideEntity(entity: ENTITY, dtoStatus: DTOStatus): ENTITY {
        if(dtoStatus == DTOStatus.Complete){
            val entityId = entity.id.value
            updateId(entityId)
        }
        executionContext.entityBacking = entity
        updateStatus(dtoStatus)
        return entity
    }
    internal fun provideData(data: DATA, dtoStatus: DTOStatus, initiator: CtxId): DATA {
        if(data.id != 0L){
            id = data.id
        }
        executionContext.provideDataModel(data, initiator)
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
                    val dataModel = executionContext.getDataModel(this)
                    if(dataModel.id == 0L){
                        val entityId = executionContext.getEntity(this).id.value
                        dataModel.id = entityId
                        tracker.dtoIdUpdated(entityId)
                    }
                }
                executionContext.notifyDTOComplete(this)
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