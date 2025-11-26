package po.misc.data.logging.models


import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredBase
import po.misc.data.logging.parts.KeyValue
import po.misc.data.logging.parts.LogTracker
import po.misc.debugging.ClassResolver

class LogMessage(
    override val context: TraceableContext,
    override val subject: String,
    override val text: String,
    override val topic: NotificationTopic,
    val  withTracker: LogTracker =  LogTracker.Disabled,
): StructuredBase(Notification(context, subject, text, topic)) {

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
        val name= info.formattedClassName
        track(name, methodName, formattedString)
    }

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