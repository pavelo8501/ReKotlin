package po.lognotify.classes.notification.models


import po.lognotify.classes.notification.JasonStringSerializable
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.enums.InfoProvider
import po.lognotify.enums.SeverityLevel
import po.lognotify.helpers.TrueHelper
import java.time.LocalDateTime

data class Notification(
    val taskName: String,
    val taskNestingLevel: Int,
    val eventType : EventType,
    val severity: SeverityLevel,
    val message: String,
    val provider: InfoProvider,
    val timestamp: LocalDateTime = LocalDateTime.now()
): JasonStringSerializable, TrueHelper {


    val taskHeaderTemplate : String
        get() {
            return if (taskNestingLevel == 0) {
                "[${taskName} | Root Task | Start | $currentTime]"
            } else {
                 "${taskName}| Nesting Level ${taskName} | Start | $currentTime]"
            }
        }

    val taskFooterTemplate : String
        get() {
            return if (taskNestingLevel == 0) {
                "[${taskName} | Root Task | Stop | $currentTime]"
            } else {
                "[${taskName}| Nesting Level : ${taskName} | Stop | $currentTime]"
            }
        }
    val taskSystemPrefix: String
        get(){

            val providerStr  = provider

            val severityEmoji : String  = SeverityLevel.emojiByValue(severity)

            return "[${currentDateTime} | ${providerStr} |  ${severityEmoji}]"
        }

    fun toFormattedString(): String{
       val  sysPrefix =   taskSystemPrefix
       return  formatSystemMsg(sysPrefix, message, severity)
    }

}