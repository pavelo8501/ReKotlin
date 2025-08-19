package po.misc.time

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

interface TimeHelper {

    fun nowTimeUtc(): Instant{
       return Instant.now()
    }

    fun nowDateTimeUtc(): ZonedDateTime {
        return ZonedDateTime.now(ZoneOffset.UTC)
    }

}