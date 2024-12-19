package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.interfaces.CanNotify
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.controls.NotificationEvent
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.DTOClass
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KClass


class DTOConfig<DATA_MODEL, ENTITY>(): CanNotify
        where DATA_MODEL : DataModel, ENTITY : LongEntity{

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
  //  val relationalBinder =  RelationBinder<DATA_MODEL, ENTITY>()

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

    inline fun <reified DTO : CommonDTO<CHILD_DATA_MODEL, CHILD_ENTITY>, reified CHILD_DATA_MODEL, reified CHILD_ENTITY>  setChildBinding(
        childDTO : DTOClass<CHILD_DATA_MODEL, CHILD_ENTITY>,
        type: BindingType,
    ) where CHILD_DATA_MODEL : DataModel, CHILD_ENTITY : LongEntity{
        val commonDTO = CommonDTO::class as KClass<CommonDTO<CHILD_DATA_MODEL, CHILD_ENTITY>>
        val child = CHILD_DATA_MODEL::class
        val a = 10
    }


//    fun setChildBindings(dtoModel: DTOClass<CHILD_DATA_MODEL, CHILD_ENTITY>, type : BindingType)
//     = childBinding.addChild(dtoModel,type)
}