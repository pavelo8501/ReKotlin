package po.db.data_service.classes.interfaces

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import po.db.data_service.controls.Notificator


interface DTOInstance{
  val className : String
  val qualifiedName : String

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

}

//interface DTOEntity<DATA: DataModel,ENTITY:LongEntity>{
//    val id:Long
//    val entityDAO : ENTITY
//}

/**
    Interface  identifying DTO Entity Class
    Part of the property data handling system
 */
interface DTOModel{
    val dataModel: DataModel
}

/**
    Interface  identifying DataModel Class
    Part of the property data handling system
 **/
interface DataModel {
    var id : Long
}

/**
    Interface for common behaviour of classes hosting Notificator Class
 **/
interface CanNotifyDepr{
    val name : String
    val notificator : Notificator
  //  fun <T>subscribe (subscriber: T, notification : ()-> Unit,  callbackFun: () -> Unit)
}