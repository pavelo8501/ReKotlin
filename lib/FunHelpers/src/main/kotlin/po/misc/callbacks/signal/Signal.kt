package po.misc.callbacks.signal

import po.misc.callbacks.CallableEventBase
import po.misc.callbacks.validator.ReactiveValidator
import po.misc.collections.lambda_map.toCallable
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.context.component.ComponentID
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.createLogProcessor
import po.misc.data.output.outputDispatcher
import po.misc.data.strings.ifNotBlank
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.SuspendedOptions
import po.misc.types.token.TypeToken


/**
 * Builder interface for configuring and registering listeners on a [Signal].
 *
 * A `Signal<T, R>` represents a strongly-typed event channel:
 *  - `T` is the payload type sent when the signal is triggered.
 *  - `R` is the result type produced by each listener.
 *
 * `SignalBuilder` exposes all supported listener registration methods.
 * The actual storage, dispatch logic, and listener removal rules
 * (e.g., for `Promise` listeners) are implemented in [Signal].
 *
 * ## Purpose
 *
 * This interface exists to:
 *  - Provide a strongly-typed DSL for listener registration.
 *  - Allow `Signal` to cleanly expose only its public API.
 *  - Enable overload disambiguation between synchronous and suspending
 *    callbacks via `LambdaOptions`, `SuspendedOptions`, and `LambdaType`.
 *
 * ## Listener Modes
 *
 * **Synchronous listeners**
 * are registered using:
 * ```
 * onSignal { (T) -> R }
 * onSignal(listener, { (T) -> R })
 * onSignal(listener, LambdaOptions, { (T) -> R })
 * ```
 *
 * **Suspending listeners**
 * are registered using:
 * ```
 * onSignal(LambdaType.Suspended) { suspend (T) -> R }
 * onSignal(listener, SuspendedOptions, suspend (T) -> R)
 * onSignal(listener, LambdaType.Suspended, suspend (T) -> R)
 * ```
 *
 * `LambdaOptions` and `SuspendedOptions` determine whether a listener
 * persists (`Listen`) or is removed after the first invocation (`Promise`).
 * These separate sealed hierarchies are required to avoid overload
 * ambiguity and ensure the correct registration path is selected.
 *
 * ## Naming
 *
 * The [signalName] function replaces the underlying component ID of the
 * associated [Signal], preserving generic type metadata while allowing
 * the signal to be meaningfully identified in diagnostics and tracing.
 *
 * @param T the type of value delivered to listeners
 * @param R the type of value returned by listeners
 */
sealed interface SignalBuilder<T: Any, R>{

    private val signal: Signal<T, R> get() = this as Signal

    /**
     * Assigns a human-readable name to this signal.
     *
     * The returned [ComponentID] becomes the signal’s new identity in logs,
     * diagnostics, tracing, and structured metadata. Generic parameter
     * information from the original signal ID is preserved and copied into
     * the new ID.
     *
     * @param name a descriptive name for the signal component
     * @return the newly assigned [ComponentID]
     */
    fun signalName(name: String):ComponentID{
        val componentID = ComponentID(signal , nameProvider = { name } )
        componentID.classInfo.genericInfoBacking.addAll(signal.componentID.classInfo.genericInfoBacking)
        signal.componentID = componentID
        return componentID
    }

    /**
     * Registers a synchronous listener associated with the signal itself
     * (the signal's own [TraceableContext]).
     */
    fun onSignal(callback: (T)-> R)

    /**
     * Registers a synchronous listener bound to a specific [TraceableContext].
     */
    fun onSignal(listener: TraceableContext, callback: (T) -> R)

    /**
     * Registers a synchronous listener with explicit invocation behavior
     * defined by [LambdaOptions] (e.g., `Listen` or `Promise`).
     */
    fun onSignal(listener: TraceableContext, options: LambdaOptions,  callback: (T) -> R)

    /**
     * Registers a suspending listener associated with the signal itself.
     *
     * The [LambdaType.Suspended] marker is required to disambiguate
     * overload resolution between suspending and non-suspending callbacks.
     */
    fun onSignal(suspended: LambdaType.Suspended, callback: suspend (T)-> R)

    /**
     * Registers a suspending listener bound to a specific context and
     * controlled by [SuspendedOptions].
     */
    fun onSignal(listener: TraceableContext, options: SuspendedOptions, callback: suspend (T) -> R)

    /**
     * Variant of suspending listener registration using [LambdaType.Suspended]
     * to explicitly select the suspending overload in ambiguous contexts.
     */
    fun onSignal(listener: TraceableContext, suspended: LambdaType.Suspended, callback: suspend (T) -> R)

    fun withValidation(predicate: (T)-> Boolean):ReactiveValidator<T>{
        return with(signal){
            registerValidator(ReactiveValidator(predicate))
        }
    }
}

/**
 * Strongly-typed event channel that dispatches values of type [T] to
 * registered listeners and collects results of type [R].
 *
 * A `Signal<T, R>` supports both synchronous and suspending listeners,
 * with optional one-shot (`Promise`) or persistent (`Listen`) behavior.
 * Listeners are uniquely associated with a [TraceableContext] and are
 * automatically removed when appropriate.
 *
 * Implements [SignalBuilder] to expose the listener registration DSL, and
 * [Component] to participate in the framework's tracing, metadata, and
 * logging systems.
 *
 * @param T the payload type passed to listeners when the signal is triggered
 * @param R the result type returned by each listener
 */
class Signal<T: Any, R>(
   paramType: TypeToken<T>,
   resultType: TypeToken<R>,
   val options: SignalOptions? = null
): CallableEventBase<T, R>(), SignalBuilder<T, R>{

    val signalName: String = if(options == null){
        "Signal"
    }else{
        "Signal[${options.name}] ${options.hostName.ifNotBlank { "on $it" }}"
    }

    override var componentID: ComponentID = ComponentID(this, setName =  signalName)
        .addParamInfo("T", paramType)
            .addParamInfo("R", resultType)

    val logProcessor: LogProcessor<Signal<T, R>, LogMessage> = createLogProcessor()

    val signal : Boolean get() = listeners.values.any { !it.isSuspended }
    val signalSuspended: Boolean get() =  listeners.values.any { it.isSuspended }

    init {
        listeners.onKeyOverwritten = {
            warn(subjectKey, messageKey(it) )
        }
    }

    /**
     * Registers a synchronous listener with explicit invocation behavior
     * defined by [LambdaOptions] (e.g., `Listen` or `Promise`).
     */
    override fun onSignal(
        listener: TraceableContext,
        options: LambdaOptions,
        callback: (T) -> R
    ){
        debug(subjectListen, messageReg("Lambda", listener) )
        listeners[listener] = callback.toCallable(options)
    }

    /**
     * Registers a non-suspending listener for this signal.
     *
     * @param listener A unique context used as the subscription key.
     *                 **If you want multiple independent listeners, always provide this parameter.**
     * @param callback The lambda that will be invoked when the signal fires.
     * Note:
     * Using an explicit `listener` ensures the subscription is not overwritten.
     */
    override fun onSignal(
        listener: TraceableContext,
        callback: (T) -> R
    ) : Unit = onSignal(listener, LambdaOptions.Listen, callback)

    /**
     * Registers a non-suspending listener using this object as the subscription key.
     *
     * Because the key is always `this`, calling this method multiple times will
     * **overwrite the previous subscription**.
     *
     * Use the overload that accepts `listener: TraceableContext` if you need stable,
     * non-conflicting subscriptions.
     */
    override fun onSignal(
        callback: (T) -> R
    ) : Unit = onSignal(this, LambdaOptions.Listen, callback)

    /**
     * Registers a suspending listener for this signal.
     *
     * @param listener A unique context used as the subscription key.
     *                 Provide this when you want the subscription to coexist with others.
     * @param options  Defines how the suspending lambda behaves (e.g., Listen, Once, etc.).
     * @param callback The suspending lambda invoked when the signal is emitted.
     *
     * This overload guarantees your subscription will not be overwritten unless the
     * same `listener` instance is reused.
     */
    override fun onSignal(
        listener: TraceableContext,
        options: SuspendedOptions,
        callback: suspend (T) -> R
    ){
        debug( subjectListen, messageReg("Suspending lambda", listener) )
        listeners[listener] = toCallable(options, callback)
    }

    /**
     * Convenience overload for registering a suspending listener with the default
     * `SuspendedOptions.Listen` behavior.
     *
     * @param listener Unique subscription key.
     *                 Required to prevent accidental overwriting.
     * @param callback Suspended lambda executed when the signal is emitted.
     */
    override fun onSignal(
        listener: TraceableContext,
        suspended: LambdaType.Suspended,
        callback: suspend (T) -> R
    ) : Unit = onSignal(listener, SuspendedOptions.Listen, callback)

    /**
     * Registers a suspending listener using `this` as the subscription key.
     *
     * Because the subscription key is always `this`, repeated calls to this method will
     * **override the previous suspending listener**.
     *
     * Use the overload that takes an explicit `listener: TraceableContext`
     * to avoid overwriting and allow multiple independent subscriptions.
     */
    override fun onSignal(
        suspended: LambdaType.Suspended,
        callback: suspend (T) -> R
    ) : Unit = onSignal(this, SuspendedOptions.Listen, callback)


    override fun notify(logMessage: LogMessage): LogMessage {
        logProcessor.logData(logMessage)
        return logMessage
    }

    override fun toString(): String = signalName

}