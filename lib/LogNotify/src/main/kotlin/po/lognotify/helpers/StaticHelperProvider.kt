package po.lognotify.helpers

import po.lognotify.enums.SeverityLevel
import po.misc.data.styles.Colour
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

//    fun makeOfColour(message: String, severity : SeverityLevel, overloadColor: Colour? = null): String{
//        return when(severity){
//            SeverityLevel.EXCEPTION -> {
//                makeOfColour(Colour.RED, message)
//            }
//            SeverityLevel.WARNING -> {
//                makeOfColour(Colour.YELLOW, message)
//            }
//            SeverityLevel.SYS_INFO ->{
//                makeOfColour(Colour.GREEN, message)
//            }
//            SeverityLevel.INFO -> {
//                message
//            }
//        }
//    }

//    fun makeOfColour(color: Colour, message: String): String{
//        return "${color.colourStr}$message${Colour.RESET.colourStr}"
//    }
}
