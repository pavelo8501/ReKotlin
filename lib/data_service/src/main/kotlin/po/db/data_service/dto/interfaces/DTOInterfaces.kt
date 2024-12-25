package po.db.data_service.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.DTOConfig

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
    fun initialize(binder : PropertyBinder?, dataModel : DataModel? = null)
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