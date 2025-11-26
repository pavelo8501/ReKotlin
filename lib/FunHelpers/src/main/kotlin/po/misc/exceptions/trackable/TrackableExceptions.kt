package po.misc.exceptions.trackable

import po.misc.coroutines.CoroutineInfo
import po.misc.coroutines.coroutineInfo
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
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
interface TrackableException{
    val exceptionTrace: ExceptionTrace
    val self : Throwable
    val contextClass: KClass<*>
    var coroutineInfo: CoroutineInfo?
}

abstract class TrackableBase(
    override val  contextClass: KClass<*>,
    message: String
): Throwable(message),TrackableException{
    override val exceptionTrace: ExceptionTrace = extractTrace()
    override var coroutineInfo: CoroutineInfo? = null
}

fun <T: TrackableException> T.provideContext(methodName: String, context: CoroutineContext):T
    = apply { coroutineInfo = context.coroutineInfo(contextClass, methodName)  }

fun <T: TrackableException> T.provideContext(context: CoroutineContext):T
    = apply { coroutineInfo = context.coroutineInfo() }
