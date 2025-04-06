package po.managedtask.helpers

import po.managedtask.enums.ColourEnum
import po.managedtask.enums.SeverityLevel
import po.managedtask.models.LogRecord
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.ZoneOffset

interface StaticsHelperProvider {

    val name: String
    val libPrefix : String

    val currentTime: String
    val currentDateTime: String
    val utcTime: String

    fun handledMsg(exception: Throwable): String
    fun formatInfo(msg: String): String
    fun formatWarn(message: String): String
    fun formatError(message: String = "", ex: Throwable? = null): String
    fun formatEcho(message: String)
    fun formatSystemMsg(message: String): String
    fun formatUnhandled(exception: Throwable): String
    fun formatLogWithIndention(logRecords : List<LogRecord>)

    fun systemPrefix(action: String): String
    fun makeOfColour(color: ColourEnum, msg: String): String
}



class StaticsHelper(override val name: String) : StaticsHelperProvider {

    override val currentTime: String
        get() = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    override val currentDateTime: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    override val utcTime: String
        get() = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    fun withIndention(message: String, depthLevel: Int) {
        val indent = "│   ".repeat(depthLevel)
        println("$indent $message")
    }

    override val libPrefix: String
        get() = makeOfColour(ColourEnum.BLUE, "[LogNotify:$currentTime @ $name]")

    override fun makeOfColour(color: ColourEnum, msg: String): String{
        return "${color.colourStr}$msg${ColourEnum.RESET.colourStr}"
    }

    override fun formatEcho(message: String) {
        val formatted = "$libPrefix message"
    }

    override fun formatInfo(message: String): String {
        val formattedString = "$libPrefix📍 $message"
        return formattedString
    }

    override fun formatSystemMsg(message: String): String {
        val formattedWarningStr = makeOfColour(ColourEnum.BLUE, message)
        val formattedString = "$libPrefix $formattedWarningStr"
        return formattedString
    }

    override fun systemPrefix(action: String): String{
        return makeOfColour(ColourEnum.BLUE, "$libPrefix/$action")
    }

    override fun formatWarn(message: String): String {
        val formattedWarningStr = makeOfColour(ColourEnum.YELLOW, "⚠️ $message")
        val formattedString = "$libPrefix $formattedWarningStr"
        return formattedString
    }

    override fun formatError(message: String, ex: Throwable?): String {

        var formattedString = makeOfColour(ColourEnum.RED, "⚠️ ${message}  Exception message:  ${ ex?.message.toString()}")
        formattedString =  "${systemPrefix(libPrefix)} $formattedString"
        println(formattedString)
        return formattedString
    }

    override fun formatUnhandled(exception: Throwable): String {
        return  makeOfColour(ColourEnum.RED, "❌ Unhandled exception: ${exception.message}")
    }

    override fun handledMsg(exception: Throwable): String =
        makeOfColour(ColourEnum.RED, "⚠️ [${name}] Handled exception, propagated: ${exception.message}")

    override fun formatLogWithIndention(logRecords : List<LogRecord>) {
        logRecords.forEach {
            var printStr = "[${it.message}:${it.timestamp}]"
            when (it.severity) {
                SeverityLevel.INFO -> {
                    printStr += it.message
                }
                SeverityLevel.WARNING -> {
                    printStr += makeOfColour(ColourEnum.YELLOW, it.message)
                }
                SeverityLevel.EXCEPTION -> {
                    printStr += makeOfColour(ColourEnum.RED, it.message)
                }
            }
        }
    }


}
