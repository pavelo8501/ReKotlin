package po.misc.callbacks.common

import po.misc.context.tracable.TraceableContext


data class ListenerResult<R: Any>(
    val listener: TraceableContext,
    val result: R
)
