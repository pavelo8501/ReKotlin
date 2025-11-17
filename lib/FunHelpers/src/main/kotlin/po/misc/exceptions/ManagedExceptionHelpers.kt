package po.misc.exceptions

import po.misc.data.logging.ContextAware
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.trackable.TrackableException


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
 *   exceptions are tagged with the correct [po.misc.context.CTXIdentity].
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
    message: String
) where TH: TrackableException, TH: Throwable {
    ExceptionLocator.raiseException<TH>(this,  message)
}

inline fun <reified TH> ContextAware.registerExceptionBuilder(
    noinline provider:(String)->TH
): Unit where TH: TrackableException, TH: Throwable = ExceptionLocator.registerExceptionBuilder(provider)
