package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.components.ContextState
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.controls.service_registry.DTOData
import kotlin.reflect.KClass


data class ModelEntityPairContainer<DATA_MODEL, ENTITY>(
    val uniqueKey : String,
//    val dataModel : AbstractDTOModel<DataModel<DATA_MODEL>, ENTITY>,
//    val entityModel : LongEntityClass<ENTITY>
)


abstract class DTOClass<DATA_MODEL, ENTITY>()
        where DATA_MODEL : DataModel, ENTITY : LongEntity{

   val dtoModelConfig = DTOConfig<DATA_MODEL, ENTITY>()


    fun update(dataModel: DATA_MODEL, entity: ENTITY) {
        onUpdateProperties?.invoke(dataModel, entity)
    }
    var onUpdateProperties :  ((dataModel: DATA_MODEL, entity: ENTITY)->Unit)? = null

   // abstract fun updateProperties(dataModel: DATA_MODEL, entity: ENTITY )

    protected abstract fun configuration()

    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) =
        dtoModelConfig.setProperties(props.toList())

    fun setDataModelConstructor(dataModelConstructor: () -> DATA_MODEL) =
        dtoModelConfig.setDataModelConstructor(dataModelConstructor)

    var state: ContextState = ContextState.UNINITIALIZED
        set(value) {
            if (field != value) {
                field = value
                when (field) {
                    ContextState.INITIALIZED -> {
                        //notificator.trigger<Unit>(NotificationEvent.ON_INITIALIZED)
                    }
                    else -> {}
                }
                println("DTOClass ${field.name}  for $dtoModelClassName ")
            }
        }

    private var dtoModelClassName: String = "undefined"

    private var _entityModel: LongEntityClass<ENTITY>? = null
    var entityModel: LongEntityClass<ENTITY>
        get() {
            return _entityModel ?: throw InitializationException(
                "EntityModel requested but not initialized",
                ExceptionCodes.LAZY_NOT_INITIALIZED
            )
        }
        set(value){
            _entityModel = value
        }

    private var _dataModel: DATA_MODEL? = null
    val dataModel: DATA_MODEL
        get() {
            return _dataModel ?: throw InitializationException(
                "DataModel requested but not initialized",
                ExceptionCodes.LAZY_NOT_INITIALIZED
            )
        }
    fun setDataModel(dataModel: DATA_MODEL) {
        _dataModel = dataModel
    }

    private var _dataModelClass: KClass<DATA_MODEL>? = null
    var dataModelClass: KClass<DATA_MODEL>
        get() {
            return _dataModelClass ?: throw InitializationException(
                "DataModelClass requested but not initialized",
                ExceptionCodes.LAZY_NOT_INITIALIZED
            )
        }
        set(value){
            this._dataModelClass = value
            dtoModelClassName = dataModelClass.qualifiedName ?: "undefined"
        }

    private var _dtoModelClass: KClass<CommonDTO<DATA_MODEL, ENTITY>>? = null
    var dtoModelClass: KClass<CommonDTO<DATA_MODEL, ENTITY>>
        get() {
            return _dtoModelClass ?: throw InitializationException(
                "DTOModelClass requested but not initialized",
                ExceptionCodes.LAZY_NOT_INITIALIZED
            )
        }
        set(value){
            this._dtoModelClass = value
        }

    fun setInitValues(
        dataModel: KClass<DATA_MODEL>,
        dtoModelClass: KClass<CommonDTO<DATA_MODEL, ENTITY>>,
        daoEntityModel: LongEntityClass<ENTITY>
    ):DTOData<DATA_MODEL, ENTITY> {
        this.dataModelClass = dataModel
        this.dtoModelClass = dtoModelClass
        this.entityModel = daoEntityModel
        this.state = ContextState.INITIALIZED
        return DTOData(dtoModelClass,dataModel,daoEntityModel)
    }

    fun initialization(
        context: DTOContext<DATA_MODEL>.() -> Unit){
        configuration()
        val dtoContext =  DTOContext<DATA_MODEL>()
        context.invoke(dtoContext)
    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }
}

inline fun <reified DTO : CommonDTO<DATA_MODEL, ENTITY>, reified DATA_MODEL, reified ENTITY>  DTOClass<DATA_MODEL, ENTITY>.initializeDTO(
    entityModel: LongEntityClass<ENTITY>,
    block: DTOConfig<DATA_MODEL, ENTITY> .() -> Unit) where DATA_MODEL : DataModel, ENTITY : LongEntity{
    val dtoData = setInitValues(DATA_MODEL::class, CommonDTO::class as KClass<CommonDTO<DATA_MODEL, ENTITY>>, entityModel)
    val a =10
}





