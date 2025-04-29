package po.exposify.classes.interfaces

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


interface ClassDTO{
  val personalName : String
    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }
}

interface DataModel {
    var id : Long
}
