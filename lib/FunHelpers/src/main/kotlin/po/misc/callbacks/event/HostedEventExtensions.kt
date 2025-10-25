package po.misc.callbacks.event

import po.misc.callbacks.common.EventHost
import po.misc.callbacks.common.EventLogRecord
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.exceptions.handling.Suspended


fun <H: EventHost, T: Any, R: Any> TraceableContext.listen(
    event: HostedEvent<H, T, R>,
    callback: H.(T)->R
) = event.onEvent(this,  callback)


fun <H: EventHost, T: Any, R: Any> TraceableContext.listen(
    suspended: Suspended,
    event: HostedEvent<H, T, R>,
    callback: suspend H.(T)->R
) = event.onEvent(this, suspended,  callback)


fun <H : EventHost, T: Any, R1: Any, R>  HostedEvent<H, T, R1>.logScope(
    topic: NotificationTopic,
    subject: String,
    block: ProceduralFlow<HostedEvent<H, T, R1>, EventLogRecord>.() -> R
): R {
    val eventLog =  EventLogRecord(this, topic, subject, "Start")
    return processor.logScope(eventLog, block)
}

fun <H : EventHost, T: Any, R1: Any, R>  HostedEvent<H, T, R1>.infoScope(
    subject: String,
    block: ProceduralFlow<HostedEvent<H, T, R1>, EventLogRecord>.() -> R
):R  = logScope(NotificationTopic.Info, subject, block)


fun <H : EventHost, T: Any, R1: Any,  R>  HostedEvent<H, T, R1>.debugScope(
    subject: String,
    block: ProceduralFlow<HostedEvent<H, T, R1>, EventLogRecord>.() -> R
):R  = logScope(NotificationTopic.Debug, subject, block)

