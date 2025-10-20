package po.misc.data.logging

import po.misc.context.tracable.NotificationTopic
import po.misc.context.tracable.TraceableContext
import po.misc.data.printable.Printable


interface LogRecord : Printable {
    val context: TraceableContext
    val topic: NotificationTopic
    val subject: String
    val text: String
}