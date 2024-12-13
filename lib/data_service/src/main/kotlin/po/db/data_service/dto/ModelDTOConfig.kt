package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.models.NotificationEvent
import po.db.data_service.models.Notificator


class ModelDTOConfig<DATA_MODEL, ENTITY>(): CanNotify where DATA_MODEL : DataModel, ENTITY : LongEntity{

    override val name: String = "ModelDTOConfig"
    override val notificator  = Notificator(this)

    var state :ContextState = ContextState.UNINITIALIZED
        set(value){
            if(field!= value){
                field = value
                when(field){
                    ContextState.INITIALIZED->{
                        notificator.trigger(NotificationEvent.ON_INITIALIZED, propertyBinder)
                    }
                    else->{

                    }
                }
            }
        }

    private val propertyBinder = DTOPropertyBinder<DATA_MODEL, ENTITY>()

    init {
        this.propertyBinder.onInitialized={
            state = ContextState.INITIALIZED

        }
    }
    var dataModelConstructor : (() -> DATA_MODEL)? = null
        private set
    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) =
        propertyBinder.setProperties(props.toList())

    fun setProperties(propertyList: List<PropertyBinding<DATA_MODEL, ENTITY, *>>) =
        propertyBinder.setProperties(propertyList)

    fun setDataModelConstructor(dataModelConstructor: () -> DATA_MODEL){
        this.dataModelConstructor = dataModelConstructor
    }
}