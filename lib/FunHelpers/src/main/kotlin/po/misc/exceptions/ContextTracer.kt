package po.misc.exceptions

import po.misc.exceptions.models.ExceptionTrace

class ContextTracer(val context: TraceableContext): Throwable("ContextTracer"), TrackableException {
    override val exceptionTrace: ExceptionTrace = metaFrameTrace(context)
}