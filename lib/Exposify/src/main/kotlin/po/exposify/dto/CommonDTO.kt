package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.dto.enums.DTOInitStatus
import po.exposify.dto.models.DTOId
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.initException
import po.exposify.exceptions.throwOperations
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.TypedCallbackRegistry
import po.misc.registries.type.TypeRegistry
import po.misc.types.TypeRecord
import java.util.UUID

abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): Identifiable, ModelDTO, TasksManaged where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    override val objectId: Long
        get() = 0

    override val dtoType :TypeRecord<DTO> get() = dtoClass.dtoType
    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY> get() =   dtoClass.config.castOrOperations<DTOConfig<DTO, DATA, ENTITY>>(this)

    val dtoId : DTOId<DTO> = DTOId(UUID.randomUUID().hashCode().toLong())
    override val contextName: String
        get() = "CommonDTO[${sourceName}#${dtoId.id}]"
    override var sourceName: String = dtoType.simpleName

    abstract override val dataModel: DATA

    val entityType: TypeRecord<ENTITY> get() = dtoClass.entityType
    val dataType:  TypeRecord<DATA> get() = dtoClass.dataType

    val logger : TaskHandler<*> get() = taskHandler()

    override var cardinality: Cardinality = Cardinality.ONE_TO_MANY

    override val daoService: DAOService<DTO, DATA, ENTITY> get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, DATA, ENTITY> get() = dtoClassConfig.dtoFactory

    private var entityParameter: ENTITY? = null
    val isEntityInserted: Boolean get() = entityParameter != null

    @PublishedApi
    internal val bindingHub: BindingHub<DTO, DATA, ENTITY> = BindingHub(this)

    var onInitializationStatusChange: ((CommonDTO<DTO, DATA, ENTITY>) -> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value) {
            if (value != field) {
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    override var id: Long
        get() = dataModel.id
        set(value){ dataModel.id = value }

    internal var trackerParameter: DTOTracker<DTO, DATA>? = null
    override val tracker: DTOTracker<DTO, DATA>
        get() {
            return trackerParameter ?: DTOTracker(this)
        }

    override fun compareTo(other: Long): Int {
        val comparison = objectId.compareTo(other)
        if (comparison != 0) {
            return objectId.toInt()
        } else {
            return 0
        }
    }

    override fun updateId(id: Long) {

    }

    internal fun getEntity():ENTITY{
        return entityParameter?:throwOperations("Requesting entity while not inserted", ExceptionCode.METHOD_MISUSED,  this)
    }

    internal fun finalizeCreation(entity: ENTITY, cardinality: Cardinality): ENTITY {
        if(entityParameter != null){

            logger.warn("finalizeCreation. Updating entityParameter when it is already set")
        }
        entityParameter = entity
        dataModel.id = entity.id.value
        this.cardinality = cardinality
        initStatus = DTOInitStatus.INITIALIZED
        return entity
    }

    internal fun provideEntity(entity: ENTITY): ENTITY {
        if(entityParameter != null){
            logger.warn("provideEntity. Updating entityParameter when it is already set")
        }
        entityParameter = entity
        dataModel.id = entity.id.value
        return entity
    }

    internal fun initialize(tracker: DTOTracker<DTO, DATA>? = null): CommonDTO<DTO, DATA, ENTITY> {
        if (tracker != null) {
            trackerParameter = tracker
        }
        initStatus = DTOInitStatus.PARTIAL_WITH_DATA
        return this
    }
}