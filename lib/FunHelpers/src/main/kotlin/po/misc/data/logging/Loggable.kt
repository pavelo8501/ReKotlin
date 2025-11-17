package po.misc.data.logging

import po.misc.context.tracable.TraceableContext
import po.misc.data.printable.Printable
import java.time.Instant


interface Loggable : Printable {
    val context: TraceableContext
    val topic: NotificationTopic
    val subject: String
    val text: String
    val created: Instant
}

