package po.test.misc.data.logging

import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.parts.LogTracker
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.createLogProcessor
import po.misc.data.logging.processor.createLogProcessor

abstract class LoggerTestBase: Component {


    class ProceduralTestComponent(var subject: String = "Processing"): Component {
        override val componentID: ComponentID = componentID("component_1")
        val processor: LogProcessor<ProceduralTestComponent, LogMessage> = createLogProcessor()
        override fun notify(logMessage: LogMessage):LogMessage {
            processor.logData(logMessage)
            return logMessage
        }
        fun emmit(subject: String,  text: String, topic: NotificationTopic = NotificationTopic.Info){
            when(topic){
                NotificationTopic.Info -> info(subject, text)
                NotificationTopic.Warning -> {

                    val warning = warning(subject, text, LogTracker.Enabled)
                    processor.logData(warning)
                }
                NotificationTopic.Exception -> warn(subject, Exception(text))
                NotificationTopic.Debug -> debug(subject,  text)
            }
        }

        fun <R: Any> resulting(result: R, beforeResult: ((ProceduralTestComponent)-> String)? = null) : R{
            val message = beforeResult?.invoke(this)
            emmit("warning", message?:"Generic warn message", NotificationTopic.Warning)
            return result
        }

        fun listResulting(count: Int, warnOnCount: Int): List<String>{
            val willWarn = if(warnOnCount > 0){
                warnOnCount.coerceAtMost(count)
            }else{ 0 }

            val result = mutableListOf<String>()
            for (i in 1..count){
                if(i == willWarn){
                    warn("TestWarning", "Emitting warning on count $i")
                    val warning = notification("TestWarning", "Emitting warning on count $i", NotificationTopic.Warning)
                    processor.logData(warning.toLogMessage())
                }
                result.add("string_$i")
            }
            return result
        }

        fun startListResulting(count: Int): List<String> = listResulting(count, 0)

        fun generateMessage(value: Int): LogMessage{
            val message = infoMsg("Subject", "Generic message # $value")
            return message
        }

        fun generateMessage(
            subjectPostfix: String,
            postfix: String,
            topic: NotificationTopic = NotificationTopic.Info
        ): LogMessage{
            val message = when(topic){
                NotificationTopic.Info ->  infoMsg("Subject_$subjectPostfix", "Generic message $postfix")
                NotificationTopic.Warning -> warning("Warning_$subjectPostfix", "Generic warning $postfix")
                NotificationTopic.Debug->{  debugMsg("Debug_$subjectPostfix", "Generic debug $postfix")  }
                NotificationTopic.Exception->{ message("Exception_$subjectPostfix", "Generic debug $postfix", topic) }
            }
            return message
        }

        fun generateMessages(
            postfix: String,
            count: Int = 1,
            topic: NotificationTopic = NotificationTopic.Info
        ): List<LogMessage> {

            val result = mutableListOf<LogMessage>()
            for (i in 1..count){
                val message = generateMessage(i.toString(),"${postfix}_$i", topic)
                result.add(message)
            }
            return result
        }
    }


    fun initialMessage(): LogMessage{
        return infoMsg("Initial", "Start")
    }

    fun generateMessage(value: Int): LogMessage{
        val message = infoMsg("Subject", "Generic message # $value")
        return message
    }


}