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

    val moduleName: String
    val libPrefix : String

    fun unhandledMsg(exception: Throwable): String
    fun handledMsg(exception: Throwable): String
    fun formatInfo(msg: String): String
    fun formatWarn(message: String): String
    fun formatError(ex: Throwable, optMessage: String = ""): String
    fun formatEcho(message: String)
    fun formatLogWithIndention(logRecords : List<LogRecord>)

}

class StaticsHelper(
    override val moduleName: String
) : StaticsHelperProvider {

    val currentTime: String
        get() = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    val currentDateTime: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    val utcTime: String
        get() = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    fun withIndention(message: String, depthLevel: Int) {
        val indent = "│   ".repeat(depthLevel)
        println("$indent $message")
    }

    override val libPrefix: String
        get() = makeOfColour(ColourEnum.BLUE, "[LogNotify:$currentTime in $moduleName]")


    fun makeOfColour(color: ColourEnum, msg: String): String = "${color.colourStr}$msg${ColourEnum.RESET.colourStr}"

    override fun formatEcho(message: String) {
        val formatted = "$libPrefix message"
        println(formatted)
    }

    override fun formatInfo(message: String): String {
        val formattedString = "$libPrefix📍 $message"
        println(formattedString)

        return formattedString
    }

    override fun formatWarn(message: String): String {
     //   currentTask.taskResult.addWarning(message)
        val formattedWarningStr = makeOfColour(ColourEnum.YELLOW, "⚠️ $message")
        val formattedString = "$libPrefix $formattedWarningStr"
        println(formattedString)
        return formattedString
    }

    override fun formatError(ex: Throwable, optMessage: String): String {
        val additionalMessage = if (!optMessage.isBlank()) {
            ".$optMessage"
        } else {
            ""
        }
        var formattedString = makeOfColour(ColourEnum.RED, "⚠️ ${ex.message.toString()}. $optMessage")
        formattedString = "$libPrefix $formattedString"
        println(formattedString)
        return formattedString
    }

    override fun unhandledMsg(exception: Throwable): String =
        "❌ [${moduleName}] Unhandled exception: ${exception.message}"

    override fun handledMsg(exception: Throwable): String =
        makeOfColour(ColourEnum.RED, "⚠️ [${moduleName}] Handled exception, propagated: ${exception.message}")

    override fun formatLogWithIndention(logRecords : List<LogRecord>) {

        logRecords.forEach {
            var printStr = "[${it.message}:${it.timestamp}]"
            when (it.severity) {
                SeverityLevel.INFO -> {
                    printStr += it.message
                    println(printStr)
                }

                SeverityLevel.WARNING -> {
                    printStr += makeOfColour(ColourEnum.YELLOW, it.message)
                    println(printStr)
                }

                SeverityLevel.EXCEPTION -> {
                    printStr += makeOfColour(ColourEnum.RED, it.message)
                    println(printStr)
                }
            }
        }
    }


}
