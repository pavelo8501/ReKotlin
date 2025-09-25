package po.misc.exceptions

import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.exceptions.models.ExceptionTrace

class ContextTracer(
    val context: TraceableContext,
    val flag: CTXResolutionFlag,
    override val message: String? = null
): Throwable("TraceableContext${message}"), TrackableException {

    override val exceptionTrace: ExceptionTrace = metaFrameTrace(context, 3, flag)
}