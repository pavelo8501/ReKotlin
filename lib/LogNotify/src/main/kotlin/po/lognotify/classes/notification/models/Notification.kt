package po.lognotify.classes.notification.models


import po.lognotify.classes.notification.JasonStringSerializable
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.sealed.InfoProvider
import po.lognotify.classes.notification.sealed.ProviderTask
import po.lognotify.classes.task.ResultantTask
import po.lognotify.classes.task.TaskIdentification
import po.lognotify.enums.ColourEnum
import po.lognotify.enums.SeverityLevel
import po.lognotify.helpers.StaticHelper
import po.misc.exceptions.CoroutineInfo
import java.time.LocalDateTime

data class Notification(
    val provider: ProviderTask,
    val eventType : EventType,
    val severity: SeverityLevel,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
): JasonStringSerializable, StaticHelper {


    private val coroutineInfo = CoroutineInfo.createInfo(provider.task .coroutineContext)
    private val task = provider.task

    private val taskHeader = mapOf(
        "task" to "${task.taskName} @ $currentDateTime",
        "task_info" to "Module: ${task.moduleName}",
        "nesting_level" to "Nesting: ${task.nestingLevel}",
        "coroutine_info" to "Coroutine Info: [HashCode: ${coroutineInfo.hashCode} Name: ${coroutineInfo.name}]",
    )

    private val taskFooter = mapOf(
        "task" to "${task.taskName} @ $currentDateTime",
        "task_info" to "Module: ${task.moduleName}",
        "nesting_level" to "Nesting: ${task.nestingLevel}",
        "elapsed" to "Completed in : ${(task.endTime - task.startTime) / 1_000_000f} ms"
    )

    private val taskPrefix = mapOf<String, String>(
        "task" to task.taskName,
        "time" to currentTime
    )

    fun getTaskHeader(): String{
        val action = "${makeOfColour(ColourEnum.MAGENTA, "Started")} "
        val resultString = makeOfColour(ColourEnum.BRIGHT_BLUE,taskHeader.map {it.value}.joinToString(" | ","","]"))
        return withIndention("[$action $resultString", task.nestingLevel)
    }

    fun getTaskFooter(): String{
        var color = ColourEnum.GREEN
        if(severity == SeverityLevel.WARNING) {
            color = ColourEnum.YELLOW
        }
        if(severity == SeverityLevel.EXCEPTION){
            color = ColourEnum.BRIGHT_RED
        }
        val action =  makeOfColour(color, "Stopped")
        val resultString = makeOfColour(ColourEnum.BRIGHT_BLUE, taskFooter.map {" ${it.value} "}.joinToString(" | ","","]"))
        return withIndention("[$action $resultString", task.nestingLevel)
    }

    fun getMessagePrefixed(): String{

        var taskString = taskPrefix.map {" ${it.value} "}.joinToString("|","[","]")
        taskString =  makeOfColour(ColourEnum.BLUE, taskString)
        taskString =  "$taskString ${SeverityLevel.emojiByValue(severity)}"
        val message = makeOfColour(message, severity, null)
        val resultString = withIndention("$taskString -> $message", task.nestingLevel)

        return resultString
    }
}