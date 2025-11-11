package po.misc.callbacks.common

import po.misc.context.tracable.TraceableContext

data class ListenerResult<R>(
    val listener: TraceableContext,
    val result: R
)
