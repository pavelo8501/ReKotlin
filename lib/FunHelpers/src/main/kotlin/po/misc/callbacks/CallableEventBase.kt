package po.misc.callbacks

import po.misc.callbacks.common.ListenerResult
import po.misc.callbacks.validator.ReactiveValidator
import po.misc.callbacks.validator.ValidationProvider
import po.misc.collections.lambda_map.CallableWrapper
import po.misc.collections.lambda_map.LambdaMap
import po.misc.collections.lambda_map.LambdaWrapper
import po.misc.collections.lambda_map.SuspendedWrapper
import po.misc.collections.lambda_map.SuspendingLambda
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.counters.SimpleJournal
import po.misc.data.helpers.firstCharUppercase
import po.misc.data.helpers.orDefault
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.TraceResolver
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.Suspended
import po.misc.functions.SuspendedOptions
import po.misc.functions.Sync
import po.misc.types.k_class.simpleOrAnon
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Base class for callable events that provides core event triggering and listener management functionality.
 *
 * [CallableEventBase] handles:
 * - Managing a collection of event listeners
 * - Triggering events to all or specific listeners
 * - Parameter validation before event dispatch
 * - Automatic cleanup of promise-based listeners
 * - Both regular and suspending event execution
 *
 * @param T The type of the event parameter
 * @param R The return type of event handlers
 *
 * @see po.misc.callbacks.event.HostedEvent
 */
abstract class CallableEventBase<T, T1,  R>(): Component {

    enum class RelayStrategy { COPY, MOVE, FORWARD }

    val journal : SimpleJournal = SimpleJournal("Event journal")

    protected open var eventName:String = "CallableEventBase"
        set(value) {
            field = value
            journal.updateName(value.firstCharUppercase())
        }

    var listenersMap: LambdaMap<T, T1, R> = LambdaMap(this)
        internal set

    internal var validator: ReactiveValidator<T1>? = null

    protected val subjectKey: String = "Key overwritten"
    protected val subjectInvoke : String = "Lambda Invoked"
    protected val subjectListen: String = "Listen Call"
    protected val subjectTrigger: String = "Trigger Call"

    protected val failedValidationMsg: String = "Call declined due to failed validation"
    protected val messageInvoke: (Any) -> String = {
        "$subjectInvoke for Class: ${it::class.simpleOrAnon} with HashCode: ${it.hashCode()}"
    }
    protected val messageKey: (Any) -> String = {
        "$subjectKey for Class: ${it::class.simpleOrAnon} with HashCode:  ${it.hashCode()}"
    }
    protected val messageReg : (String, TraceableContext)-> String = {callbackType, context->
        if(context === this){
            "$callbackType initialized"
        }else{
            "$callbackType initialized for ${ClassResolver.instanceName(context)}"
        }
    }

    protected abstract fun listenersApplied(lambdaType: LambdaType)

    protected fun checkRemoval(receiver: TraceableContext, callable: CallableWrapper<T, T1, R>): TraceableContext? {
        return if (callable.options is LambdaOptions.Promise || callable.options is SuspendedOptions.Promise) {
            receiver
        } else {
            null
        }
    }

    protected fun subscribe(receiver: TraceableContext, callable: CallableWrapper<T, T1, R>, other: CallableEventBase<*, *, *>? = null): TraceableContext? {
        val existent =  listenersMap.putIfAbsent(receiver, callable)
        if (existent != null){
            warn("Signal relay", "Events for subscriber $receiver were overwritten ${other.orDefault{ "by $it" } }")
        }
        when(callable){
            is SuspendingLambda<*, *> ->  listenersApplied(Suspended)
            else -> listenersApplied(Sync)
        }
        return existent
    }

    /**
     * Registers a validator for event parameters.
     * The validator will be called before triggering any events to validate the parameter.
     *
     * @param validator The [ReactiveValidator] to use for parameter validation
     * @return The registered validator for method chaining
     *
     * @see ReactiveValidator
     * @see CallableEventBase.trigger
     */
    fun registerValidator(validator: ReactiveValidator<T1>): ReactiveValidator<T1>{
        this.validator = validator
        return validator
    }

    /**
     * Triggers the event for all registered listeners with the given value.
     *
     * @param value The event parameter to pass to all listeners
     * @return A list of [ListenerResult] containing the listener and their return value
     *
     */
    fun trigger(value: T, parameter: T1): List<ListenerResult<R>> {
        val result = mutableListOf<ListenerResult<R>>()
        val forRemoval = mutableListOf<TraceableContext>()

        if(validator?.validate(parameter) == false){
            info(subjectTrigger, failedValidationMsg)
            return emptyList()
        }
        listenersMap.lambdaMap.forEach { (listener, callable) ->
            val callResult = callable.invoke(value, parameter)
            checkRemoval(listener, callable)?.let {
                forRemoval.add(it)
            }
            result.add(ListenerResult(listener, callResult))
        }

        forRemoval.forEach {
            listenersMap .remove(it)
        }
        return result
    }

    /**
     * Triggers the event for a specific listener with the given value.
     *
     * @param listener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @return The result from the listener, or null if the listener doesn't exist or validation fails
     *
     */
    open fun trigger(listener: TraceableContext, value: T, parameter: T1): R? {
        if(validator?.validate(parameter) == false){
            info(subjectTrigger, failedValidationMsg)
            return null
        }
        return listenersMap.lambdaMap[listener]?.let { callable ->
            val result = callable.invoke(value, parameter)
            checkRemoval(listener, callable)?.let {
                listenersMap.removeLambda(it)
            }
            result
        }
    }

    /**
     * Triggers the event for a specific listener with external validation.
     *
     * @param listener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @param validator External validation provider to validate the event parameter
     * @return The result from the listener, or null if validation fails or listener doesn't exist
     */
    fun trigger(listener: TraceableContext, value: T,  parameter: T1,  validator: ValidationProvider<T>) : R? {
        if(validator.validate(value)){
            return trigger(listener, value, parameter)
        }
        info(subjectTrigger, failedValidationMsg)
        return null
    }

    /**
     * Triggers the event for all listeners with external validation.
     *
     * @param value The event parameter to pass to all listeners
     * @param validator External validation provider to validate the event parameter
     * @return A list of [ListenerResult] containing the listener and their return value
     */
    open fun trigger(value: T,  parameter: T1, validator: ValidationProvider<T>):List<ListenerResult<R>>{
        if(validator.validate(value)){
            return trigger(value, parameter)
        }
        info(subjectTrigger, failedValidationMsg)
        return emptyList()
    }

    /**
     * Triggers the event for a specific listener with the given value using suspending execution.
     *
     * @param listener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @param suspended Marker parameter to indicate suspending execution
     * @return The result from the listener, or null if the listener doesn't exist or validation fails
     */
    open suspend fun trigger(listener: TraceableContext, value: T,  parameter: T1,  suspended: Suspended): R? {
        if(validator?.validate(parameter, suspended) == false){
            info(subjectTrigger, failedValidationMsg)
            return null
        }
        return listenersMap.suspendedMap[listener]?.let { callable ->
            val result = callable.invoke(value, parameter)
            checkRemoval(listener, callable)?.let {
                listenersMap.removeSuspended(it)
            }
            result
        }
    }

    /**
     * Triggers the event for all registered listeners with the given value using suspending execution.
     *
     * @param value The event parameter to pass to all listeners
     * @param suspended Marker parameter to indicate suspending execution
     * @return A list of [ListenerResult] containing the listener and their return value
     */
    open suspend fun trigger(value: T,  parameter: T1,  suspended: Suspended): List<ListenerResult<R>> {
        val result = mutableListOf<ListenerResult<R>>()
        val forRemoval = mutableListOf<TraceableContext>()
        if(validator?.validate(parameter, suspended) == false){
            info(subjectTrigger, failedValidationMsg)
            return emptyList()
        }
        listenersMap.suspendedMap.forEach { (listener, callable) ->
            val callResult = callable.invoke(value, parameter)
            checkRemoval(listener, callable)?.let {
                forRemoval.add(it)
            }
            result.add(ListenerResult(listener, callResult))
        }
        forRemoval.forEach {
            listenersMap.removeSuspended(it)
        }
        return result
    }

    /**
     * Triggers the event for all listeners with external validation using suspending execution.
     *
     * @param value The event parameter to pass to all listeners
     * @param suspended Marker parameter to indicate suspending execution
     * @param validator External validation provider to validate the event parameter
     * @return A list of [ListenerResult] containing the listener and their return value
     */
    open suspend fun trigger(value: T,  parameter: T1, suspended: Suspended, validator: ValidationProvider<T>):List<ListenerResult<R>>{
        if(validator.validate(value, suspended)){
            return trigger(value, parameter, suspended)
        }
        info(subjectTrigger, failedValidationMsg)
        return emptyList()
    }

    /**
     * Triggers the event for a specific listener with external validation using suspending execution.
     *
     * @param listener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @param suspended Marker parameter to indicate suspending execution
     * @param validator External validation provider to validate the event parameter
     * @return The result from the listener, or null if validation fails or listener doesn't exist
     */
    open suspend fun trigger(listener: TraceableContext, value: T, parameter: T1, suspended: Suspended, validator: ValidationProvider<T>) : R? {
        if(validator.validate(value, suspended)){
            return trigger(listener, value, parameter,  suspended)
        }
        info(subjectTrigger, failedValidationMsg)
        return null
    }

    fun relay(other: CallableEventBase<T, T1,  R>, strategy: RelayStrategy){
        journal.info("Relaying events to $other. Strategy: $strategy")
        when(strategy){
            RelayStrategy.MOVE ->{
                listenersMap.listenerEntries.forEach {
                    other.subscribe(it.key, it.value, this)
                }
                journal.info("${listenersMap.size} subscribers moved to $other.")
                listenersMap.clear()
            }
            RelayStrategy.FORWARD ->{
                listenersMap.listenerEntries.forEach {
                    other.subscribe(it.key, it.value, this)
                }
                journal.info("${listenersMap.size} forwarded moved to $other.")
            }
            RelayStrategy.COPY ->{
                listenersMap.listenerEntries.forEach {
                    other.subscribe(it.key, it.value, this)
                }
                journal.info("${listenersMap.size} copied to $other.")
            }
        }
    }
}