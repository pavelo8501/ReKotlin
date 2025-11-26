package po.misc.callbacks.signal

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.SuspendedOptions


/**
 * Registers a synchronous listener on the given [signal], using this
 * [TraceableContext] as the listener identity.
 *
 * This extension is a convenience wrapper around
 * `signal.onSignal(listener, options, callback)` and is typically used as:
 *
 * ```
 * context.listen(mySignal) { data ->
 *     // handle data
 * }
 * ```
 *
 * The [options] parameter defaults to [LambdaOptions.Listen], meaning the
 * listener will persist until explicitly removed or the signal decides to
 * remove it (e.g., when using a `Promise` listener).
 *
 * @param signal the signal to listen to
 * @param options behavior modifier (`Listen` or `Promise`)
 * @param callback the synchronous listener function
 */
fun <T: Any, R> TraceableContext.listen(
    signal: Signal<T, R>,
    options: LambdaOptions = LambdaOptions.Listen,
    callback: (T)->R
): Unit = signal.onSignal(this, options,  callback)

/**
 * Registers a suspending listener on the given [signal], using this
 * [Component] as the listener identity.
 *
 * This extension forwards to
 * `signal.onSignal(listener, options, suspend (T) -> R)` and is intended
 * for coroutine-based listeners where asynchronous processing is required.
 *
 * Example:
 * ```
 * context.listen(mySignal, SuspendedOptions.Listen) { data ->
 *     delay(50)
 *     handle(data)
 * }
 * ```
 *
 * @param signal the signal to listen to
 * @param options behavior modifier (`Listen` or `Promise`)
 * @param callback the suspending listener function
 */
fun <T: Any, R> TraceableContext.listen(
    signal: Signal<T, R>,
    options: SuspendedOptions,
    callback: suspend (T)->R
): Unit = signal.onSignal(this, options, callback)


/**
 * Registers a suspending listener on the given [signal], using this
 * [TraceableContext] as the listener identity, where the listener mode is
 * explicitly selected using [LambdaType.Suspended].
 *
 * This overload exists *solely* to avoid Kotlin overload ambiguity when
 * the compiler cannot infer whether the listener is synchronous or
 * suspending based on context alone.
 *
 * It defaults to [SuspendedOptions.Listen] for convenience.
 *
 * Example:
 * ```
 * context.listen(mySignal, LambdaType.Suspended) { data ->
 *     performAsyncWork(data)
 * }
 * ```
 *
 * @param suspended explicit marker used to select the suspending overload
 * @param signal the signal to listen to
 * @param callback the suspending listener function
 */
fun <T: Any, R> TraceableContext.listen(
    signal: Signal<T, R>,
    suspended: LambdaType.Suspended,
    callback: suspend (T)->R
): Unit = signal.onSignal(this, SuspendedOptions.Listen, callback)


