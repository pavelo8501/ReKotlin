package po.db.data_service.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.DTOConfig


interface DAOWInstance{

}

interface DTOModelClass<DATA,ENTITY> where  ENTITY : LongEntity, DATA: DataModel{
    var className : String
    val configuration : DTOConfig<DATA,ENTITY>?
}

interface DTOEntity<DATA: DataModel,ENTITY:LongEntity>{
    val id:Long
    val injectedDataModel: DATA
    val entityDAO : ENTITY
}


/**
    Interface  identifying DTO Entity Class
    Part of the property data handling system
 */
interface DTOModel : DAOWInstance {
    val dataModel: DataModel
}

/**
    Interface  identifying DataModel Class
    Part of the property data handling system
 **/
interface DataModel : DAOWInstance {
    var id : Long
}

/**
    Interface for common behaviour of classes hosting Notificator Class
 **/
interface CanNotify{
    val name : String
    val notificator : Notificator
  //  fun <T>subscribe (subscriber: T, notification : ()-> Unit,  callbackFun: () -> Unit)
}