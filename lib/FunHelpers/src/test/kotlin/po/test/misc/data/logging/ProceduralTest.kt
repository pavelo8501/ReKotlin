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
import po.misc.data.logging.processor.logProcessor

abstract class LoggerTestBase {

    protected class ProceduralTestComponent(var subject: String = "Processing"): Component {

        override val componentID: ComponentID = componentID("component_1")
        val processor: LogProcessor<ProceduralTestComponent, LogMessage> = logProcessor()
        override fun notify(logMessage: LogMessage) {
            processor.logData(logMessage)
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
    }

}