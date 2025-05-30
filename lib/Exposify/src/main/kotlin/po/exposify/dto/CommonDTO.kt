package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.dto.enums.DTOInitStatus
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.interfaces.ComponentType
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.classes.task.TaskHandler
import po.misc.registries.type.TypeRegistry

abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY>
        get() {
            return dtoClass.config.castOrOperationsEx<DTOConfig<DTO, DATA, ENTITY>>("dtoClassConfig uninitialized")
        }

    val registryRecord: TypeRegistry get() = dtoClass.config.registry
    val logger : TaskHandler<*> get()= dtoClass.logger

    override val dtoName: String get() = "CommonDTO[${registryRecord.getSimpleName(ComponentType.DTO)}]"
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY

    abstract override var dataModel: DATA

    override val daoService: DAOService<DTO, DATA, ENTITY> get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, DATA, ENTITY> get() = dtoClassConfig.dtoFactory

    private var entityParameter: ENTITY? = null
    internal val entity: ENTITY
        get() = entityParameter.getOrOperationsEx("Requesting entity while not inserted", ExceptionCode.METHOD_MISUSED)
    val isEntityInserted: Boolean  get() = entityParameter != null

    @PublishedApi
    internal val bindingHub: BindingHub<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity> = BindingHub(this)

    var onInitializationStatusChange: ((CommonDTO<DTO, DATA, ENTITY>) -> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value) {
            if (value != field) {
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    override var id: Long = 0
        get() = dataModel.id

    internal var trackerParameter: DTOTracker<DTO, DATA>? = null
    override val tracker: DTOTracker<DTO, DATA>
        get() {
            return trackerParameter ?: DTOTracker(this)
        }

    fun provideInsertedEntity(entity: ENTITY): ENTITY {
        entityParameter = entity
        dataModel.id = entity.id.value
        initStatus = DTOInitStatus.INITIALIZED
        return entity
    }

    fun createByData(): CommonDTO<DTO, DATA, ENTITY> {
        bindingHub.createByData()
        dtoClass.registerDTO(this)
        return this
    }

    internal fun initialize(tracker: DTOTracker<DTO, DATA>? = null): CommonDTO<DTO, DATA, ENTITY> {
        if (tracker != null) {
            trackerParameter = tracker
        }
        initStatus = DTOInitStatus.PARTIAL_WITH_DATA
        return this
    }
}