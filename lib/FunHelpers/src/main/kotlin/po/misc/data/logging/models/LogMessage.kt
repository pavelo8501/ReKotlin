package po.misc.data.logging.models


import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredBase
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.parts.KeyValue
import po.misc.data.logging.parts.LogTracker
import po.misc.debugging.ClassResolver
import po.misc.types.helpers.simpleOrAnon

class LogMessage(
    override val context: TraceableContext,
    override val subject: String,
    override val text: String,
    override val topic: NotificationTopic,
    val  withTracker: LogTracker =  LogTracker.Disabled,
): StructuredBase(Notification(context, topic, subject, text)) {

    constructor(
         loggable: Loggable,
         withTracker: LogTracker =  LogTracker.Disabled
    ):this(loggable.context, loggable.subject, loggable.text, loggable.topic, withTracker){
        tracker = withTracker
    }
    override var tracker: LogTracker = LogTracker.Disabled

    override fun track(context: TraceableContext, methodName: String){
        if(tracker == LogTracker.Disabled){ return }
        val info = ClassResolver.classInfo(context)
        val name= info.instanceName?:info.simpleName
        track(name, methodName, formattedString)
    }

    override fun toString(): String = "${topic.name} message by ${context::class.simpleOrAnon} Text: $text"

    companion object {
        fun track(contextName: String, methodName: String, loggableHeader : String){
            buildString {
                appendLine(loggableHeader)
                appendLine(KeyValue("Name",  contextName))
                appendLine(KeyValue("Method",  methodName))
            }
        }
    }

}