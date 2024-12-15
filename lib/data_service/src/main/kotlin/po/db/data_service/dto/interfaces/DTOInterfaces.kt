package po.db.data_service.dto.interfaces

import po.db.data_service.controls.Notificator

/*
    Wide marker for wrapper classes
 */
interface DAOWInstance{

}


/*
    Interface used to identify Class as a DTO Entity
    Part of the property data handling system
 */
interface DTOEntityMarker<DATA_MODEL, ENTITY> : DAOWInstance {
    var id : Long
    val dataModelClassName : String
}

/*
    Interface  identifying DTO Entity Class
    Part of the property data handling system
 */
interface DTOModel : DAOWInstance {
    //val dtoModel : CommonDTO<*,*>
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