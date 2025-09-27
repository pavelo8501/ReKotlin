package po.misc.exceptions

import kotlinx.coroutines.currentCoroutineContext
import po.misc.coroutines.CoroutineInfo
import po.misc.coroutines.coroutineInfo
import po.misc.data.helpers.output
import po.misc.data.logging.ContextAware
import po.misc.data.logging.ContextAware.ExceptionLocator
import po.misc.exceptions.models.ExceptionTrace
import kotlin.reflect.KClass


/**
 * ## Managed Exception Helpers
 *
 * These helpers wrap [ExceptionLocator] to provide a clean API for raising
 * and reusing context-aware exceptions inside any [ContextAware].
 *
 * ### registerExceptionBuilder
 * Register a custom exception builder for later reuse.
 *
 * ```
 * class MyCustomException(message: String) : Throwable(message), TrackableException {
 *     override val exceptionTrace = ExceptionTrace.empty()
 * }
 *
 * // Register once (e.g. in initialization code)
 * registerExceptionBuilder<MyCustomException> { msg -> MyCustomException(msg) }
 * ```
 *
 * After registration, `raiseException<MyCustomException>()` can be called
 * without repeating constructor logic.
 *
 * ---
 *
 * ### raiseManagedException
 * Raise a generic context-managed exception, supplying an optional [traceProvider].
 *
 * ```
 * raiseManagedException("Something went wrong") { trace ->
 *     trace.dump()
 * }
 * ```
 *
 * ---
 *
 * ### raiseException<T : Throwable & TrackableException>
 * Raise a specific exception type.
 *
 * - Overload 1: With a custom trace handler
 *   ```
 *   raiseException<MyCustomException>("Invalid state") { trace ->
 *       trace.logToDB()
 *   }
 *   ```
 *
 * - Overload 2: Without a handler (simpler, uses registered builder if available)
 *   ```
 *   raiseException<MyCustomException>("Missing data")
 *   ```
 *
 * ---
 *
 * ### Key Points
 * - **Reusable builders:** `registerExceptionBuilder` lets you register exception
 *   constructors once. Subsequent calls to `raiseException<T>()` can reuse them.
 * - **Trace integration:** All raised exceptions automatically capture
 *   [ExceptionTrace] via [ExceptionLocator].
 * - **Context binding:** The raising [ContextAware] is always passed, ensuring
 *   exceptions are tagged with the correct [CTXIdentity].
 *
 * ---
 *
 * ### Typical Usage
 * ```
 * class Worker : ContextAware {
 *     override val identity = asIdentity()
 *     override val emitter = logEmitter()
 *
 *     fun doWork() {
 *         try {
 *             raiseException<MyCustomException>("Bad input")
 *         } catch (ex: MyCustomException) {
 *             warn("Caught custom exception: ${ex.message}")
 *         }
 *     }
 * }
 * ```
 */

fun ContextAware.raiseManagedException(
    message: String,
    traceProvider:((ExceptionTrace)-> Unit)? = null
){
    ExceptionLocator.raiseManagedException(this,  message, traceProvider)
}

inline fun <reified TH> ContextAware.raiseException(
    message: String,
    crossinline traceProvider:(ExceptionTrace)-> Unit
) where TH: TrackableException, TH: Throwable {
    ExceptionLocator.raiseException<TH>(this,  message, traceProvider)
}

inline fun <reified TH> ContextAware.raiseException(
    message: String
) where TH: TrackableException, TH: Throwable {
    ExceptionLocator.raiseException<TH>(this,  message)
}

inline fun <reified TH> ContextAware.registerExceptionBuilder(
    noinline provider:(String)->TH
): Unit where TH: TrackableException, TH: Throwable = ExceptionLocator.registerExceptionBuilder(provider)




inline fun <reified TH: Throwable> ContextAware.registerHandler(
    noinline block: (TH)-> Unit
): Boolean = ExceptionLocator.registerHandler<TH>(block)



inline fun <reified TH: Throwable, reified T: Any> ContextAware.registerHandler(
    parameter: T,
    noinline block: suspend (TH)-> Unit
): Boolean = ExceptionLocator.registerHandler<TH, T>(ThrowableContainer(parameter, block))



inline fun <R: Any> ContextAware.delegateIfThrow(block:()-> R):R?{
    try {
      return  block()
    }catch (th: Throwable){
        ExceptionLocator.handle(th)
        return null
    }
}

suspend fun <R: Any> ContextAware.delegateIfThrow(methodName: String,  block: suspend ()-> R):R?{

    try {
      return  block()
    }catch (th: Throwable){
        if(th is TrackableScopedException){
            th.coroutineInfo = th.contextClass.coroutineInfo(methodName)
        }
        ExceptionLocator.handleSuspending(th)
        return null
    }
}


