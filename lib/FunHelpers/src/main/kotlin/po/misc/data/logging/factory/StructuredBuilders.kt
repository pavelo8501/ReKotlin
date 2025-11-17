package po.misc.data.logging.factory

import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.models.Notification
import po.misc.data.logging.log_subject.LogSubject

fun Notification.toLogMessage(): LogMessage{
    return LogMessage(this)
}

fun LogSubject.toLogMessage(ctx: TraceableContext, notificationTopic: NotificationTopic? = null): LogMessage{
    return LogMessage( Notification(ctx, notificationTopic?:topic, subjectName, subjectText))
}

fun Loggable.toLogMessage(): LogMessage{
    return LogMessage(this)
}

