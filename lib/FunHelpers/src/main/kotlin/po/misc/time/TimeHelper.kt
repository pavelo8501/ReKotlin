package po.misc.time

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface TimeHelper {

    fun nowTimeUtc(): Instant = Instant.now()
    fun nowDateTimeUtc(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

    fun nowTime(hoursOffset: Int): OffsetDateTime = Instant.now().atOffset(ZoneOffset.ofHours(hoursOffset))
    fun nowLocalDateTime(): ZonedDateTime = ZonedDateTime.now()

    fun hoursFormated(instant: Instant): String{
        return instant.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }
    fun hoursFormated(offsetTime: OffsetDateTime): String{
        return offsetTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }
    fun dateFormated(instant: Instant): String{
      return instant.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd-MM-yyyy / HH:mm:ss"))
    }
    fun dateFormated(offsetTime: OffsetDateTime): String{
        return offsetTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy / HH:mm:ss"))
    }
    fun dateFormated(zonedDateTime: ZonedDateTime): String{
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy / HH:mm:ss"))
    }
    fun Instant.havePassed(nextTime: Instant): Duration{
        return (nextTime.toEpochMilli() - toEpochMilli()).milliseconds
    }

    fun createTimeStamp(name: String):ExecutionTimeStamp{
       return ExecutionTimeStamp(name)
    }

}