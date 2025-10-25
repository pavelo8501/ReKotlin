package po.misc.data.processors

import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.Notification
import po.misc.data.logging.procedural.ProceduralEntry

interface LoggerContext {


    fun  TraceableContext.loggable(topic: NotificationTopic, subject: String, text: String): Loggable{
        return Notification(this, topic, subject, text)
    }


    fun Loggable.toProceduralEntry(stepBadge: String? = null): ProceduralEntry{
        val badge = stepBadge?:"[${subject.take(4).uppercase()}]"
        return  ProceduralEntry(text, badge)
    }

}