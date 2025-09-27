package po.misc.exceptions

import po.misc.context.TraceableContext
import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.exceptions.models.ExceptionTrace
import kotlin.reflect.KClass

class ContextTracer(
    val context: TraceableContext,
    val flag: CTXResolutionFlag,
    override val message: String? = null
): Throwable("TraceableContext${message}"), TrackableException {

    override val self: ContextTracer = this
    override val contextClass: KClass<*> = context::class

    override val exceptionTrace: ExceptionTrace = metaFrameTrace(contextClass, 3)
}