package po.misc.exceptions

import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo
import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.trackable.TrackableException
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KFunction


open class ContextTracer(
    val context: TraceableContext,
    val flag: CTXResolutionFlag = CTXResolutionFlag.Resolvable,
    override val message: String? = null
): Throwable("TraceableContext${message}"), TrackableException {

    override val self: ContextTracer = this
    override val contextClass: KClass<*> = context::class
    override var coroutineInfo: CoroutineInfo? = null
    override val exceptionTrace: ExceptionTrace = extractTrace()
}

open class Tracer(
    override val message: String = "",
    var printImmediately: Boolean = true,
): Throwable() {
    val created: Instant = Instant.now()

    val firstTraceElement : StackTraceElement get() = stackTrace.first()

    fun resolveTrace(context: TraceableContext):ExceptionTrace = extractTrace()
}

interface TracerOptions{
    object Trace : TracerOptions
    object InstanceInfo : TracerOptions
}


fun TraceableContext.trace():  ExceptionTrace{
    return Tracer().extractTrace()
}

fun TraceableContext.trace(options: TraceOptions):  ExceptionTrace{
    val tracer = Tracer()
   val trace = when(options){
        is TraceCallSite ->{

            tracer.extractTrace(options)
        }
    }
    return trace
}


fun TraceableContext.trace(classInfo: TracerOptions.InstanceInfo): InstanceInfo{
    val tracer =  Tracer()
    val trace =  tracer.resolveTrace(this)
    val instanceInfo = ClassResolver.resolveInstance(this).addTraceInfo(trace)
    return instanceInfo
}

fun TraceableContext.trace(throwable: Throwable): ExceptionTrace{
    return throwable.extractTrace()
}


