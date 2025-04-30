package po.lognotify.classes.notification.models


import po.lognotify.classes.notification.JasonStringSerializable
import po.lognotify.classes.notification.NotificationProvider
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.sealed.DataProvider
import po.lognotify.enums.ColourEnum
import po.lognotify.enums.SeverityLevel
import po.lognotify.helpers.StaticHelper
import po.misc.time.ExecutionTimeStamp
import java.time.LocalTime

data class Notification(
    val provider: DataProvider,
    val eventType : EventType,
    val severity: SeverityLevel,
    val message: String,
    val timestamp:  LocalTime = LocalTime.now()
): JasonStringSerializable, StaticHelper {

    private val coroutineInfo = provider.coroutineInfo?:"N/A"



    val header
        get() = """
           [Started ${provider.name} @ $timestamp | Module: ${provider.module} | Nesting: ${provider.name} | Coroutine Info: ${coroutineInfo} ]
        """.trimIndent()

    val footer
        get() = """
            [Stopped  ${provider.name} @ $timestamp | Module: ${provider.module} | Nesting: ${provider.name} | Completed in: ${provider.executionTime?.elapsed} ]
        """.trimIndent()


//        private val taskHeader = mapOf(
//        "task" to "${task.taskName} @ $currentDateTime",
//        "task_info" to "Module: ${task.moduleName}",
//        "nesting_level" to "Nesting: ${task.nestingLevel}",
//        "coroutine_info" to "Coroutine Info: [HashCode: ${coroutineInfo.hashCode} Name: ${coroutineInfo.name}]",
//    )

//    private val taskFooter = mapOf(
//        "task" to "${task.taskName} @ $currentDateTime",
//        "task_info" to "Module: ${task.moduleName}",
//        "nesting_level" to "Nesting: ${task.nestingLevel}",
//        "elapsed" to "Completed in : ${timestamp.elapsed} ms"
//    )
//
//    private val taskPrefix = mapOf<String, String>(
//        "task" to task.taskName,
//        "time" to currentTime
//    )

   // fun getHeader(): String{
//        val action = "${makeOfColour(ColourEnum.MAGENTA, "Started")} "
//        val resultString = makeOfColour(ColourEnum.BRIGHT_BLUE,taskHeader.map {it.value}.joinToString(" | ","","]"))
//        return withIndention("[$action $resultString", task.nestingLevel)
      //  return header
   // }

   // fun getFooter(): String{
//        var color = ColourEnum.GREEN
//        if(severity == SeverityLevel.WARNING) {
//            color = ColourEnum.YELLOW
//        }
//        if(severity == SeverityLevel.EXCEPTION){
//            color = ColourEnum.BRIGHT_RED
//        }
//        val action =  makeOfColour(color, "Stopped")
//        val resultString = makeOfColour(ColourEnum.BRIGHT_BLUE, taskFooter.map {" ${it.value} "}.joinToString(" | ","","]"))
//        return withIndention("[$action $resultString", task.nestingLevel)
     //   return footer
   // }

    fun getMessagePrefixed(): String{


        val result = """"
           ${makeOfColour(
                ColourEnum.BRIGHT_BLUE,
                "${SeverityLevel.emojiByValue(severity)}[${provider.name} @ ${currentTime}]")}->${makeOfColour(message,severity,null) }
        """.trimIndent()



//        var taskString = taskPrefix.map {" ${it.value} "}.joinToString("|","[","]")
//        taskString =  makeOfColour(ColourEnum.BLUE, taskString)
//        taskString =  "$taskString ${SeverityLevel.emojiByValue(severity)}"
//        val message = makeOfColour(message, severity, null)
//        val resultString = withIndention("$taskString -> $message", task.nestingLevel)

        return result
    }
}