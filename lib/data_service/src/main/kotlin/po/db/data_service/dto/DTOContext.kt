package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.interfaces.CanNotify
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.controls.NotificationEvent
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.BindingType
import po.db.data_service.dto.components.ContextState
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.controls.service_registry.DTOData
import kotlin.reflect.KClass



class DTOContext<DATA_MODEL, ENTITY>() : CanNotify where   DATA_MODEL : DataModel, ENTITY : LongEntity {

    var state: ContextState = ContextState.UNINITIALIZED
        set(value) {
            if (field != value) {
                field = value
                when (field) {
                    ContextState.INITIALIZED -> {
                        notificator.trigger<Unit>(NotificationEvent.ON_INITIALIZED)
                    }

                    else -> {}
                }
            }
        }

    val dtoModelConfig = DTOConfig<DATA_MODEL, ENTITY>()

    override val name = "DTOClassOuterContext"
    override var notificator = Notificator(this)

    init {

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
        dataModelClass = dataModel
        this.dtoModelClass = dtoModelClass
        entityModel = daoEntityModel
        state = ContextState.INITIALIZED
        return DTOData(dtoModelClass,daoEntityModel,dataModel)
    }

    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) =
        dtoModelConfig.setProperties(props.toList())

    fun setDataModelConstructor(dataModelConstructor: () -> DATA_MODEL) =
        dtoModelConfig.setDataModelConstructor(dataModelConstructor)

    fun <CHILD_DATA_MODEL : DataModel,  CHILD_ENTITY: LongEntity>setChildBinding(
        childDTO :  DTOClass<CHILD_DATA_MODEL, CHILD_ENTITY>,
        type: BindingType
    ){
        //childDTO.initialization()
    }
}