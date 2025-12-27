package po.misc.exceptions

import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.coroutines.coroutineInfo
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.debugging.stack_tracer.ExceptionTrace
import po.misc.debugging.stack_tracer.extractTrace
import java.time.Instant
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass


/**
 * Base contract for exceptions that can be traced and resolved in a structured way.
 *
 * Unlike plain [Throwable], this interface introduces:
 * - [exceptionTrace] – a structured trace of the exception’s origin,
 * - [self] – a strongly typed reference to the implementing throwable,
 * - [contextClass] – the class that provides contextual information for this exception.
 *
 * ### Why `self`?
 * Since `Throwable` is a class (not an interface), we can’t directly inherit it
 * and keep everything generic.
 * The `self` reference makes it possible to safely use the concrete throwable
 * type in generic resolution/utility functions without unsafe casts.
 *
 * ### Example
 * ```kotlin
 * class ContextTracer(
 *     val context: TraceableContext,
 *     val flag: CTXResolutionFlag,
 *     override val message: String? = null
 * ) : Throwable("TraceableContext${message}"), TrackableException {
 *
 *     override val self: ContextTracer = this
 *     override val contextClass: KClass<*> = context::class
 *     override val exceptionTrace: ExceptionTrace = metaFrameTrace(contextClass, 3)
 * }
 * ```
 */
interface TraceException{
    val trace: ExceptionTrace
    var coroutineInfo: CoroutineInfo?
}

abstract class ContextTracer(
    message: String = "",
    val options: TraceOptions = TraceOptions.Default,
    val classifier: PackageClassifier? = null
): Throwable("TraceableContext${message}"), TraceException {

    val created: Instant = Instant.now()
    val firstElement : StackTraceElement get() = stackTrace.first()

    override var coroutineInfo: CoroutineInfo? = null

    override var trace : ExceptionTrace  = extractTrace(options, analyzeDepth = 30, classifier)

    open  fun createTrace(options: TraceOptions, classifier: PackageClassifier? = null):ExceptionTrace{
        val exTrace =  this.extractTrace(options, analyzeDepth = 50,  classifier)
        trace =  exTrace
        return exTrace
    }
}


open class Tracer(
     message: String,
     options: TraceOptions = TraceOptions.Default,
     classifier: PackageClassifier? = null
): ContextTracer(message, options, classifier){

    constructor(
        options: TraceOptions = TraceOptions.Default,
        classifier: PackageClassifier? = null
    ):this("",  options, classifier)

    override fun createTrace(options: TraceOptions, classifier: PackageClassifier?):ExceptionTrace{
        val exTrace =  this.extractTrace(options, analyzeDepth = 50,  classifier)
        trace =  exTrace
        return exTrace
    }
}


fun TraceableContext.extractTrace(
    options: TraceOptions = TraceOptions.Default,
    classifier: PackageClassifier? = null
):  ExceptionTrace{
    return Tracer(options, classifier).trace
}

fun TraceableContext.extractTrace(
    throwable: Throwable,
    options: TraceOptions = TraceOptions.Default
): ExceptionTrace = throwable.extractTrace(options)


fun <T: ContextTracer> T.provideContext(methodName: String, contextClass:  KClass<*>, context: CoroutineContext):T
        = apply { coroutineInfo = context.coroutineInfo(contextClass, methodName)  }

fun <T: ContextTracer> T.provideContext(context: CoroutineContext):T
        = apply { coroutineInfo = context.coroutineInfo() }



