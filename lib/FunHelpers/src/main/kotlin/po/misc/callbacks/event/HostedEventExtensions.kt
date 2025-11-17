package po.misc.callbacks.event

import po.misc.callbacks.common.EventHost
import po.misc.context.tracable.TraceableContext
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.SuspendedOptions


/**
 * Extension functions for registering event listeners in a more idiomatic and concise way.
 *
 * These extensions provide a clean DSL-like syntax for attaching listeners to [HostedEvent] instances,
 * making event registration more readable and reducing boilerplate code.
 */

/**
 * Registers a regular event listener with default options.
 *
 * This extension provides a more natural syntax for event listener registration
 * by making the receiver context the explicit listener.
 *
 * @param H The type of the event host
 * @param T The type of the event parameter
 * @param R The return type of the event handler
 * @param event The [HostedEvent] to listen to
 * @param callback The event handler function to be called when the event is triggered
 *
 * @see HostedEvent.onEvent
 */
fun <H: EventHost, T: Any, R> TraceableContext.listen(
    event: HostedEvent<H, T, R>,
    callback: H.(T) -> R
): Unit = event.onEvent(this,  callback)


/**
 * Registers a regular event listener with specific options.
 *
 * Allows fine-grained control over listener behavior through [LambdaOptions].
 *
 * @param H The type of the event host
 * @param T The type of the event parameter
 * @param R The return type of the event handler
 * @param event The [HostedEvent] to listen to
 * @param options Configuration options for the listener behavior
 * @param callback The event handler function to be called when the event is triggered
 *
 * @see LambdaOptions
 * @see HostedEvent.onEvent
 */
fun <H: EventHost, T: Any, R> TraceableContext.listen(
    event: HostedEvent<H, T, R>,
    options: LambdaOptions = LambdaOptions.Listen,
    callback: H.(T) -> R
): Unit = event.onEvent(this, options,  callback)


/**
 * Registers a suspending event listener with specific options.
 *
 * Use this extension when you need to perform asynchronous operations
 * in your event handler.
 *
 * @param H The type of the event host
 * @param T The type of the event parameter
 * @param R The return type of the event handler
 * @param event The [HostedEvent] to listen to
 * @param options Configuration options for the suspending listener behavior
 * @param callback The suspending event handler function to be called when the event is triggered
 *
 * @see SuspendedOptions
 * @see HostedEvent.onEvent
 */
fun <H: EventHost, T: Any, R> TraceableContext.listen(
    event: HostedEvent<H, T, R>,
    options: SuspendedOptions = SuspendedOptions.Listen,
    callback: suspend H.(T) -> R
): Unit = event.onEvent(this, options,  callback)

/**
 * Registers a suspending event listener using the [LambdaType.Suspended] marker.
 *
 * This extension uses the type-safe marker pattern to disambiguate between
 * regular and suspending function overloads, providing clear intent at the call site.
 *
 * @param H The type of the event host
 * @param T The type of the event parameter
 * @param R The return type of the event handler
 * @param event The [HostedEvent] to listen to
 * @param suspended Marker parameter to explicitly indicate suspending function type
 * @param callback The suspending event handler function to be called when the event is triggered
 *
 * @see LambdaType.Suspended
 *
 * @example
 * ```kotlin
 * context.listen(event, LambdaType.Suspended) { param ->
 *     val data = fetchDataAsync(param)
 *     processResult(data)
 * }
 * ```
 */
fun <H: EventHost, T: Any, R> TraceableContext.listen(
    event: HostedEvent<H, T, R>,
    suspended: LambdaType.Suspended,
    callback: suspend H.(T) -> R
): Unit = event.onEvent(this, suspended, callback)



