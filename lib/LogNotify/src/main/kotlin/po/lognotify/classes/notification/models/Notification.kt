package po.lognotify.classes.notification.models


import po.lognotify.classes.notification.JasonStringSerializable
import po.lognotify.classes.notification.NotificationProvider
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.sealed.DataProvider
import po.lognotify.classes.notification.sealed.ProviderTask
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

  //  private val coroutineInfo = provider.coroutineInfo?:"N/A"


    val nestingLevel : String
        get(){
            return if(provider is ProviderTask){
                if(provider.nestingLevel == 0){
                    "R"
                }else{
                    provider.nestingLevel.toString()
                }
            }else{
                "N/A"
            }
        }

    val coroutineInfo : String get(){
        if(provider is ProviderTask){
            if(provider.nestingLevel == 0){
               return "[Coroutine Info: ${provider.coroutineInfo}]"
            }
        }
        return  ""
    }


    val header : String
        get() = """
           ${makeOfColour(ColourEnum.BRIGHT_BLUE,"[${nestingLevel} Start")} ${provider.name} @ $timestamp | Module: ${provider.module}]
           $coroutineInfo
        """.trimIndent()

    val footer : String
        get() = """
           ${makeOfColour("[${nestingLevel} Stop", severity)} ${provider.name} @ $timestamp | Module: ${provider.module} | Completed in: ${provider.executionTime?.elapsed} ]
        """.trimIndent()

    fun getMessagePrefixed(): String{
        val result = """
           ${makeOfColour(
                ColourEnum.BRIGHT_BLUE,
                "${SeverityLevel.emojiByValue(severity)}[${provider.name} @ ${currentTime}]")}->${makeOfColour(message,severity,null) }
        """.trimIndent()
        return result
    }
}