package po.lognotify.helpers

import po.misc.data.console.ColourEnum
import po.lognotify.enums.SeverityLevel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.ZoneOffset

interface StaticHelper{

    fun timestamp(): LocalTime {
        return LocalTime.now()
    }

     val currentTime: String
        get() = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

     val currentDateTime: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

     val utcTime: String
        get() = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    fun withIndention(message: String, depthLevel: Int, indentionSymbol: String = " "): String {
            val indent = indentionSymbol.repeat(depthLevel)
            return "$indent $message"
        }

    fun makeOfColour(message: String, severity : SeverityLevel, overloadColor: ColourEnum? = null): String{
        return when(severity){

            SeverityLevel.EXCEPTION -> {
                makeOfColour(ColourEnum.RED, message)
            }
            SeverityLevel.WARNING -> {
                makeOfColour(ColourEnum.YELLOW, message)
            }
            SeverityLevel.SYS_INFO ->{
                makeOfColour(ColourEnum.GREEN, message)
            }
            SeverityLevel.INFO -> {
                message
            }
        }
    }

    fun makeOfColour(color: ColourEnum, message: String): String{
        return "${color.colourStr}$message${ColourEnum.RESET.colourStr}"
    }
}
