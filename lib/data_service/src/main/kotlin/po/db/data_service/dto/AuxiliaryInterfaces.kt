package po.db.data_service.dto

import po.db.data_service.models.Notificator

/*
    Wide marker for wrapper classes
 */
interface DAOWInstance{

}


/*
    Interface used to identify Class as a DTO Entity
    Part of the property data handling system
 */
interface DTOEntityMarker<DATA_MODEL, ENTITY> :  DAOWInstance{
    var id : Long
    val dataModelClassName : String
    fun setEntityDAO(entity : ENTITY)
}

/*
    Interface  identifying DTO Entity Class
    Part of the property data handling system
 */
interface DTOModel : DAOWInstance {
    val dtoModel : CommonDTO<*,*>
    val dataModel: DataModel
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
  //  fun <T>subscribe (subscriber: T, notification : ()-> Unit,  callbackFun: () -> Unit)
}