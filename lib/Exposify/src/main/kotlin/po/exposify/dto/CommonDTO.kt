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
import po.exposify.dto.models.Component
import po.exposify.dto.models.ComponentType
import po.exposify.dto.models.DTOId
import po.exposify.dto.models.SourceObject
import po.exposify.dto.models.componentInstance
import po.exposify.exceptions.InitException
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.classes.task.TaskHandler
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.TypedCallbackRegistry
import po.misc.registries.type.TypeRegistry
import po.misc.types.TypeRecord
import java.util.UUID

abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): Identifiable,  ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    enum class Events(override val value: Int): ValueBased{
        OnParentAttached(1)
    }

    override val dtoType :TypeRecord<DTO> = registry.getRecord<DTO, InitException>(SourceObject.DTO)
    val dtoName: String = dtoType.simpleName
    val dtoId : DTOId<DTO> = DTOId(UUID.randomUUID().hashCode().toLong())
    override val completeName: String
        get() = "CommonDTO[${dtoName}#${dtoId.id}]"
    override val componentName: String get() = "CommonDTO"

    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY>
        get() {
            return dtoClass.config.castOrOperationsEx<DTOConfig<DTO, DATA, ENTITY>>("dtoClassConfig uninitialized")
        }

    val registry: TypeRegistry get() = dtoClass.config.registry
    abstract override val dataModel: DATA

    val entityType: TypeRecord<ENTITY>
        get() = registry.getRecord<ENTITY, InitException>(SourceObject.Entity)
    val dataType:  TypeRecord<DATA>
        get() = registry.getRecord<DATA, InitException>(SourceObject.Data)

    val logger : TaskHandler<*> get()= dtoClass.logger

    override var cardinality: Cardinality = Cardinality.ONE_TO_MANY

    override val daoService: DAOService<DTO, DATA, ENTITY> get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, DATA, ENTITY> get() = dtoClassConfig.dtoFactory

    private var entityParameter: ENTITY? = null
    val isEntityInserted: Boolean get() = entityParameter != null

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

    override var id: Long
        get() = dataModel.id
        set(value){ dataModel.id = value }

    internal var trackerParameter: DTOTracker<DTO, DATA>? = null
    override val tracker: DTOTracker<DTO, DATA>
        get() {
            return trackerParameter ?: DTOTracker(this)
        }

    private val notificator: TypedCallbackRegistry<CommonDTO<ModelDTO, DataModel, LongEntity>, Unit> = TypedCallbackRegistry()


    init {
        notificator.onNewSubscription = { key, subscriber->
            logger.info("New subscription for key: $key  by: ${subscriber.completeName}")
        }
        notificator.onKeyOverwrite = {keyStr, subscriber->
            logger.warn("Key: $keyStr was overwritten by ${subscriber.completeName}")
        }
        notificator.onBeforeTrigger = {key, subscriber, value ->
            val message = """
                About to trigger key: ${key.value} for subscriber ${subscriber.completeName}
                Trigger payload: ${value::class.simpleName}
            """.trimIndent()
            logger.info(message)
        }
        notificator.onAfterTriggered = {
            logger.info("Triggers count: $it")
        }
    }


    internal fun <F_DTO, FD, FE> setForeignDTO(
        foreign: CommonDTO<F_DTO, FD, FE>
    ) where F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
    {
        logger.info("foreign received cast ${foreign::class.simpleName}")
        val castedToInterface = foreign.castOrOperationsEx<CommonDTO<ModelDTO, DataModel, LongEntity>>()
        logger.info("foreign after cast ${foreign::class.simpleName}")
        notificator.triggerForAll(Events.OnParentAttached, castedToInterface)

    }

    internal fun subscribe(
        subscriber: Identifiable,
        eventType: Events,
        callback:(CommonDTO<ModelDTO, DataModel, LongEntity>)-> Unit)
    {
        notificator.subscribe(subscriber, eventType, callback)
    }

    internal fun getEntity(requesting: Identifiable):ENTITY{
       return if(isEntityInserted){
            entityParameter.getOrOperationsEx(withIdentification("Requesting entity while not inserted"), ExceptionCode.METHOD_MISUSED)
        }else{
            val requestedBy = requesting.withIdentification("Entity was requested by")
            val message = "${withIdentification("Requesting entity while not inserted")}${requestedBy}"
            entityParameter.getOrOperationsEx(message, ExceptionCode.METHOD_MISUSED)
        }
    }


    internal fun finalizeCreation(entity: ENTITY, cardinality: Cardinality): ENTITY {
        if(entityParameter != null){
            logger.warn(withIdentification("finalizeCreation. Updating entityParameter when it is already set"))
        }
        entityParameter = entity
        dataModel.id = entity.id.value
        this.cardinality = cardinality
        initStatus = DTOInitStatus.INITIALIZED
        return entity
    }

    internal fun provideEntity(entity: ENTITY): ENTITY {
        if(entityParameter != null){
            logger.warn(withIdentification("provideEntity. Updating entityParameter when it is already set"))
        }
        entityParameter = entity
        dataModel.id = entity.id.value
        return entity
    }

//    fun createByData(): CommonDTO<DTO, DATA, ENTITY> {
//        bindingHub.createByData()
//        dtoClass.registerDTO(this)
//        return this
//    }

    internal fun initialize(tracker: DTOTracker<DTO, DATA>? = null): CommonDTO<DTO, DATA, ENTITY> {
        if (tracker != null) {
            trackerParameter = tracker
        }
        initStatus = DTOInitStatus.PARTIAL_WITH_DATA
        return this
    }
}