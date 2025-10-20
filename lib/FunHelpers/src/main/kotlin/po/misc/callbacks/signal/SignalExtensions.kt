package po.misc.callbacks.signal

import po.misc.context.tracable.TraceableContext




fun <T: Any, R: Any> TraceableContext.listen(signa: Signal<T, R>, callback: (T)->R) = signa.onEvent(this,  callback)