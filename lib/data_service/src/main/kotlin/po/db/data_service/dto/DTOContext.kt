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
import po.db.data_service.dto.components.ContextState
import po.db.data_service.models.CommonDTO
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

    val configuration = DTOConfig<DATA_MODEL, ENTITY>()

    override val name = "DTOClassOuterContext"
    override var notificator = Notificator(this)

    init {

    }

    private var dtoModelClassName: String = "undefined"

    private var _entityModel: LongEntityClass<ENTITY>? = null
    val entityModel: LongEntityClass<ENTITY>
        get() {
            return _entityModel ?: throw InitializationException(
                "EntityModel requested but not initialized",
                ExceptionCodes.LAZY_NOT_INITIALIZED
            )
        }

    fun setEntityModel(entityModel: LongEntityClass<ENTITY>) {
        _entityModel = entityModel
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
    val dataModelClass: KClass<DATA_MODEL>
        get() {
            return _dataModelClass ?: throw InitializationException(
                "DataModelClass requested but not initialized",
                ExceptionCodes.LAZY_NOT_INITIALIZED
            )
        }

    fun setDataModelClass(clazz: KClass<DATA_MODEL>) {
        _dataModelClass = clazz
        dtoModelClassName = dataModelClass.qualifiedName ?: "undefined"
    }

    private var _dtoModelClass: KClass<CommonDTO<DATA_MODEL, ENTITY>>? = null
    val dtoModelClass: KClass<CommonDTO<DATA_MODEL, ENTITY>>
        get() {
            return _dtoModelClass ?: throw InitializationException(
                "DTOModelClass requested but not initialized",
                ExceptionCodes.LAZY_NOT_INITIALIZED
            )
        }

    fun setDTOModelClass(clazz: KClass<CommonDTO<*, *>>) {
        _dtoModelClass = clazz as KClass<CommonDTO<DATA_MODEL, ENTITY>>
    }

    fun setInitValues(
            dataModel: KClass<DATA_MODEL>,
            dtoModel: KClass<CommonDTO<*, *>>,
            daoEntityModel: LongEntityClass<ENTITY>
    ) {
        setDataModelClass(dataModel)
        setEntityModel(daoEntityModel)
        state = ContextState.INITIALIZED
    }

    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) =
        configuration.setProperties(props.toList())

    fun setDataModelConstructor(dataModelConstructor: () -> DATA_MODEL) =
        configuration.setDataModelConstructor(dataModelConstructor)


//    fun <C_DAT, C_ENT>setChildBindings(
//        block:   <DATA_MODEL, ENTITY, C_DAT, C_ENT>.()->Unit
//        ) where C_DAT : DataModel, C_ENT : LongEntity {
//
//        val a =10
//        }
}