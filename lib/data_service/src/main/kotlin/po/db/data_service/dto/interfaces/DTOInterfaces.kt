package po.db.data_service.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.models.interfaces.DTOEntity

/*
    Wide marker for wrapper classes
 */
interface DAOWInstance{

}

interface DTOModelClass<ENTITY> where  ENTITY : LongEntity{
    var className : String
    val configuration : DTOConfig<ENTITY>?
}

interface DTOEntity{
    val id:Long
    val dataModel: DataModel
    val className : String
    fun initialize(binder : PropertyBinder, dataModel : DataModel?)
}

interface DTOModelV2 : DTOEntity {
    val dataModel: DataModel
    val className : String
}

/*
    Interface  identifying DTO Entity Class
    Part of the property data handling system
 */
interface DTOModel : DAOWInstance {
    val dataModel: DataModel
    val className : String
}

/*
    Interface  identifying DataModel Class
    Part of the property data handling system
 */
interface DataModel : DAOWInstance {
    var id : Long
}



/*
    Interface for common behaviour of classes hosting Notificator Class
 */
interface CanNotify{
    val name : String
    val notificator : Notificator
  //  fun <T>subscribe (subscriber: T, notification : ()-> Unit,  callbackFun: () -> Unit)
}