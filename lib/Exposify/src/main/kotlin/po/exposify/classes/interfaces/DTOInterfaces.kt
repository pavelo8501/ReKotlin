package po.exposify.classes.interfaces

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


interface DTOInstance{
  val personalName : String
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
