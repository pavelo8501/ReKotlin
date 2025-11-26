package po.misc.callbacks.event

import po.misc.callbacks.CallableEventBase
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.common.EventLogRecord
import po.misc.callbacks.validator.ReactiveValidator
import po.misc.collections.lambda_map.toCallable
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.log_provider.LogProvider
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Verbosity
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.createLogProcessor
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.SuspendedOptions
import po.misc.types.token.TypeToken


/**
 * Builder interface for configuring and registering listeners on a [HostedEvent].
 *
 * A `HostedEvent<H, T, R>` represents a strongly-typed event channel:
 *  - 'H' is type of the event host that will receive events
 *  - `T` is the payload type sent when the signal is triggered.
 *  - `R` is the result type produced by each listener.
 *
 * `HostedEventBuilder` exposes all supported listener registration methods.
 * The actual storage, dispatch logic, and listener removal rules
 * (e.g., for `Promise` listeners) are implemented in [HostedEvent].
 *
 * ## Purpose
 *
 * This interface exists to:
 *  - Provide a strongly-typed DSL for listener registration.
 *  - Allow `HostedEvent` to cleanly expose only its public API.
 *  - Enable overload disambiguation between synchronous and suspending
 *    callbacks via `LambdaOptions`, `SuspendedOptions`, and `LambdaType`.
 *
 * ## Listener Modes
 *
 * **Synchronous listeners**
 * are registered using:
 * ```
 * onEvent { H.(T) -> R }
 * onEvent(listener, { H.(T) -> R })
 * onEvent(listener, LambdaOptions, { H.(T) -> R })
 * ```
 *
 * **Suspending listeners**
 * are registered using:
 * ```
 * onEvent(LambdaType.Suspended) { suspend H.(T) -> R }
 * onEvent(listener, SuspendedOptions, suspend H.(T) -> R)
 * onEvent(listener, LambdaType.Suspended, suspend H.(T) -> R)
 * ```
 *
 * `LambdaOptions` and `SuspendedOptions` determine whether a listener
 * persists (`Listen`) or is removed after the first invocation (`Promise`).
 * These separate sealed hierarchies are required to avoid overload
 * ambiguity and ensure the correct registration path is selected.
 *
 * ## Naming
 *
 * The [eventName] function replaces the underlying component ID of the
 * associated [HostedEvent], preserving generic type metadata while allowing
 * the event to be meaningfully identified in diagnostics and tracing.
 *
 * @param H the type of  event host that will receive events
 * @param T the type of value delivered to listeners
 * @param R the type of value returned by listeners
 */
sealed interface HostedEventBuilder<H : EventHost, T: Any, R> {

    private val event  get() = this as HostedEvent<H, T, R>

    fun eventName(name: String):ComponentID{

        val id =  ComponentID(event, verbosity = Verbosity.Info, nameProvider = { name } )

       // id.classInfo.genericInfoBacking.addAll(event.componentID.classInfo.genericInfoBacking)
        event.componentID = id
        return id
    }

    fun onEvent(callback: H.(T) -> R)
    fun onEvent(listener: TraceableContext, callback: H.(T) -> R)
    fun onEvent(listener: TraceableContext, options: LambdaOptions, callback: H.(T) -> R)

    fun onEvent(suspended: LambdaType.Suspended, callback: suspend H.(T) -> R)
    fun onEvent(listener: TraceableContext, suspended: LambdaType.Suspended, callback: suspend H.(T) -> R)
    fun onEvent(listener: TraceableContext, options: SuspendedOptions, callback: suspend H.(T) -> R)

    fun withValidation(predicate: (T)-> Boolean):ReactiveValidator<T>{
        return with(event){
            registerValidator(ReactiveValidator(predicate))
        }
    }
}


/**
 * A hosted event that manages event listeners and handles event triggering with validation support.
 *
 * [HostedEvent] provides a robust event system that:
 * - Supports both regular and suspending event handlers
 * - Includes validation for event parameters
 * - Tracks listeners through [TraceableContext]
 * - Provides comprehensive logging capabilities
 * - Supports method chaining through [HostedEventBuilder] interface
 *
 * @param H The type of the event host that will receive events
 * @param T The type of the event parameter passed to listeners
 * @param R The return type of the event handlers
 * @property event Returns true if there are any active (non-suspended) event listeners
 * @property eventSuspended Returns true if there are any suspended event listeners
 *
 */
class HostedEvent<H: EventHost, T : Any, R>(
    internal val host: H,
    paramType: TypeToken<T>,
    resultType: TypeToken<R>
): CallableEventBase<T, R>(),  HostedEventBuilder<H, T, R>, LogProvider {

    override var componentID: ComponentID =
        componentID("HostedEvent", Verbosity.Warnings).addParamInfo("T", paramType).addParamInfo("R", resultType)

    override val logProcessor: LogProcessor<HostedEvent<H, T, R>, LogMessage> = createLogProcessor()
    

    val event: Boolean get() = listeners.values.any { !it.isSuspended }
    val eventSuspended: Boolean get() = listeners.values.any { it.isSuspended }

    init {
        listeners.onKeyOverwritten = {
            warn(subjectKey, messageKey(it))
        }
    }

    /**
     * Registers a regular event listener with specific options.
     *
     * @param listener The traceable context that will own this listener
     * @param options Configuration options for the listener behavior
     * @param callback The event handler function that will be called when the event is triggered
     *
     * @see LambdaOptions
     */
    override fun onEvent(
        listener: TraceableContext,
        options: LambdaOptions,
        callback: H.(T) -> R
    ) {

        debug(subjectListen, messageReg("Lambda", listener))
        listeners[listener] = callback.toCallable(host, options)
    }

    /**
     * Registers a regular event listener with default listening options.
     *
     * @param listener The traceable context that will own this listener
     * @param callback The event handler function
     */
    override fun onEvent(
        listener: TraceableContext,
        callback: H.(T) -> R
    ): Unit = onEvent(listener, LambdaOptions.Listen, callback)

    /**
     * Registers a regular event listener owned by this event instance.
     *
     * @param callback The event handler function
     */
    override fun onEvent(callback: H.(T) -> R): Unit = onEvent(this, LambdaOptions.Listen, callback)

    /**
     * Registers a suspending event listener with specific options.
     *
     * @param listener The traceable context that will own this listener
     * @param options Configuration options for the suspending listener
     * @param callback The suspending event handler function
     *
     * @see SuspendedOptions
     */
    override fun onEvent(
        listener: TraceableContext,
        options: SuspendedOptions,
        callback: suspend H.(T) -> R
    ) {
        debug(subjectListen, messageReg("Suspending lambda", listener))
        listeners[listener] = host.toCallable(options, callback)
    }

    /**
     * Registers a suspending event listener with default listening options.
     *
     * @param listener The traceable context that will own this listener
     * @param suspended Marker parameter to indicate suspending function type
     * @param callback The suspending event handler function
     */
    override fun onEvent(
        listener: TraceableContext,
        suspended: LambdaType.Suspended,
        callback: suspend H.(T) -> R
    ): Unit = onEvent(listener, SuspendedOptions.Listen, callback)

    /**
     * Registers a suspending event listener owned by this event instance.
     *
     * @param suspended Marker parameter to indicate suspending function type
     * @param callback The suspending event handler function
     */
    override fun onEvent(
        suspended: LambdaType.Suspended,
        callback: suspend H.(T) -> R
    ): Unit = onEvent(this, SuspendedOptions.Listen, callback)

}
