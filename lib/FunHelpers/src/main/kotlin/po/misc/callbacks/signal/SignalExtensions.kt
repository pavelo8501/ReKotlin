package po.misc.callbacks.signal


import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.handling.Suspended

fun <T: Any, R: Any> TraceableContext.listen(
    signa: Signal<T, R>, callback: (T)->R
) = signa.onEvent(this,  callback)


fun <T: Any, R: Any> TraceableContext.listen(
    suspended: Suspended,
    signa: Signal<T, R>, callback: (T)->R
) = signa.onEvent(this, suspended, callback)
