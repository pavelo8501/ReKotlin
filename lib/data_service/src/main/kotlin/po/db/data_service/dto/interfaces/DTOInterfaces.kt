package po.db.data_service.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.DTOConfigV2
import po.db.data_service.models.interfaces.DTOEntity

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


interface DTOModelClass {
    var className : String
    val configuration : DTOConfigV2?

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

interface DTOEntity<DATA_MODEL> : DTOModel, DataModel  {

}


/*
    Interface for common behaviour of classes hosting Notificator Class
 */
interface CanNotify{
    val name : String
    val notificator : Notificator
  //  fun <T>subscribe (subscriber: T, notification : ()-> Unit,  callbackFun: () -> Unit)
}