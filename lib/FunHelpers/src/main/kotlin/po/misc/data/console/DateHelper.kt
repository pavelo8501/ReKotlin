package po.misc.data.console

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter



interface DateHelper{

    fun timestamp(): LocalTime = LocalTime.now()

    val currentTime: String
        get() = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))


    val nowTime: String
        get() = currentTime

    val currentDateTime: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    val utcTime: String
        get() = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
}

