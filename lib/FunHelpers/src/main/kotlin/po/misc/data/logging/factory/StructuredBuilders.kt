package po.misc.data.logging.factory

import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.models.Notification
import po.misc.data.logging.log_subject.LogSubject
import po.misc.data.logging.log_subject.RunSubject
import po.misc.data.logging.procedural.ProceduralRecord

fun Notification.toLogMessage(): LogMessage{
    return LogMessage(this)
}

fun LogSubject.toLogMessage(ctx: TraceableContext, notificationTopic: NotificationTopic? = null): LogMessage{
    return LogMessage(ctx, subjectName, subjectText,  notificationTopic?:topic)
}

fun RunSubject.toLogMessage(): LogMessage{
    return LogMessage(context, subjectName, subjectText, NotificationTopic.Info)
}


fun Loggable.toLogMessage(): LogMessage{
    return when(this){
        is LogMessage -> this
        is Notification -> LogMessage(this)
        is ProceduralRecord -> this.logRecord
        else -> LogMessage(this)
    }
}

