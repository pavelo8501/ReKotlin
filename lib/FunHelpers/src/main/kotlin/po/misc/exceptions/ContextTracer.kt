package po.misc.exceptions

import po.misc.context.TraceableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.trackable.TrackableException
import kotlin.reflect.KClass

open class ContextTracer(
    val context: TraceableContext,
    val flag: CTXResolutionFlag,
    override val message: String? = null
): Throwable("TraceableContext${message}"), TrackableException {

    override val self: ContextTracer = this
    override val contextClass: KClass<*> = context::class
    override var coroutineInfo: CoroutineInfo? = null

    override val exceptionTrace: ExceptionTrace = metaFrameTrace(contextClass, 3, flag)
}