package po.misc.exceptions

import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.ClassInfo
import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.trackable.TrackableException
import java.time.Instant
import kotlin.reflect.KClass


open class ContextTracer(
    val context: TraceableContext,
    val flag: CTXResolutionFlag = CTXResolutionFlag.Resolvable,
    override val message: String? = null
): Throwable("TraceableContext${message}"), TrackableException {

    override val self: ContextTracer = this
    override val contextClass: KClass<*> = context::class
    override var coroutineInfo: CoroutineInfo? = null

    override val exceptionTrace: ExceptionTrace = extractTrace(context)
}


class StackTracer(
    override val message: String = "",
    var printImmediately: Boolean = true,

): Throwable() {
    val created: Instant = Instant.now()

    fun resolveTrace(context: TraceableContext, block: ClassInfo.()-> ClassInfo): ClassInfo{
       val trace =  extractTrace(context).bestPick
       val info =  ClassResolver.classInfo(context)
       return info.addTraceInfo(trace).block()
    }


}