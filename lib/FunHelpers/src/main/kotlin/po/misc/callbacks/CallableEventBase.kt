package po.misc.callbacks

import po.misc.callbacks.common.ListenerResult
import po.misc.callbacks.validator.ReactiveValidator
import po.misc.callbacks.validator.ValidationProvider
import po.misc.collections.lambda_map.CallableWrapper
import po.misc.collections.lambda_map.LambdaMap
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.TraceResolver
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.SuspendedOptions
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
abstract class CallableEventBase <T: Any, R>(): Component {

    internal val listeners: LambdaMap<T, R> = LambdaMap(this)
    internal var validator: ReactiveValidator<T>? = null

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

    protected fun checkRemoval(
        receiver: TraceableContext,
        callable: CallableWrapper<T, R>
    ): TraceableContext? {
        return if (callable.options is LambdaOptions.Promise || callable.options is SuspendedOptions.Promise) {
            receiver
        } else {
            null
        }
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
    fun registerValidator(validator: ReactiveValidator<T>): ReactiveValidator<T>{
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
    fun trigger(value: T): List<ListenerResult<R>> {
        val result = mutableListOf<ListenerResult<R>>()
        val forRemoval = mutableListOf<TraceableContext>()

        if(validator?.validate(value) == false){
            info(subjectTrigger, failedValidationMsg)
            return emptyList()
        }

        listeners.forEach { (listener, callable) ->
            val callResult = callable.invoke(value)
            checkRemoval(listener, callable)?.let {
                forRemoval.add(it)
            }
            result.add(ListenerResult(listener, callResult))
        }
        forRemoval.forEach {
            listeners.remove(it)
        }
        return result
    }

    /**
     * Triggers the event for a specific listener with the given value.
     *
     * @param forListener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @return The result from the listener, or null if the listener doesn't exist or validation fails
     *
     */
    fun trigger(
        forListener: TraceableContext,
        value: T
    ): R? {
        if(validator?.validate(value) == false){
            info(subjectTrigger, failedValidationMsg)
            return null
        }
        return listeners[forListener]?.let { callable ->
            val result = callable.invoke(value)
            checkRemoval(forListener, callable)?.let {
                listeners.remove(it)
            }
            result
        }
    }

    /**
     * Triggers the event for a specific listener with the given value using suspending execution.
     *
     * @param forListener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @param suspended Marker parameter to indicate suspending execution
     * @return The result from the listener, or null if the listener doesn't exist or validation fails
     */
    suspend fun trigger(
        forListener: TraceableContext,
        value: T,
        suspended: LambdaType.Suspended
    ): R? {
        if(validator?.validate(value, suspended) == false){
            info(subjectTrigger, failedValidationMsg)
            return null
        }
        return listeners[forListener]?.let { callable ->
            val result = callable.invokeSuspending(value)
            checkRemoval(forListener, callable)?.let {
                listeners.remove(it)
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
    suspend fun trigger(
        value: T,
        suspended: LambdaType.Suspended
    ): List<ListenerResult<R>> {
        val result = mutableListOf<ListenerResult<R>>()
        val forRemoval = mutableListOf<TraceableContext>()
        if(validator?.validate(value, suspended) == false){
            info(subjectTrigger, failedValidationMsg)
            return emptyList()
        }
        listeners.forEach { (listener, callable) ->
            val callResult = callable.invokeSuspending(value)
            checkRemoval(listener, callable)?.let {
                forRemoval.add(it)
            }
            result.add(ListenerResult(listener, callResult))
        }
        forRemoval.forEach {
            listeners.remove(it)
        }
        return result
    }

    /**
     * Triggers the event for a specific listener with external validation.
     *
     * @param forListener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @param validator External validation provider to validate the event parameter
     * @return The result from the listener, or null if validation fails or listener doesn't exist
     */
    fun trigger(
        forListener: TraceableContext,
        value: T,
        validator: ValidationProvider<T>
    ):R?{
        if(validator.validate(value)){
            return trigger(forListener, value)
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
    fun trigger(
        value: T,
        validator: ValidationProvider<T>
    ):List<ListenerResult<R>>{
        if(validator.validate(value)){
            return trigger(value)
        }
        info(subjectTrigger, failedValidationMsg)
        return emptyList()
    }

    /**
     * Triggers the event for all listeners with external validation using suspending execution.
     *
     * @param value The event parameter to pass to all listeners
     * @param suspended Marker parameter to indicate suspending execution
     * @param validator External validation provider to validate the event parameter
     * @return A list of [ListenerResult] containing the listener and their return value
     */
    suspend fun trigger(
        value: T,
        suspended: LambdaType.Suspended,
        validator: ValidationProvider<T>
    ):List<ListenerResult<R>>{

        if(validator.validate(value, suspended)){
            return trigger(value, suspended)
        }
        info(subjectTrigger, failedValidationMsg)
        return emptyList()
    }

    /**
     * Triggers the event for a specific listener with external validation using suspending execution.
     *
     * @param forListener The specific listener to trigger the event for
     * @param value The event parameter to pass to the listener
     * @param suspended Marker parameter to indicate suspending execution
     * @param validator External validation provider to validate the event parameter
     * @return The result from the listener, or null if validation fails or listener doesn't exist
     */
    suspend fun trigger(
        forListener: TraceableContext,
        value: T,
        suspended: LambdaType.Suspended,
        validator: ValidationProvider<T>
    ):R? {
        if(validator.validate(value, suspended)){
            return trigger(forListener, value, suspended)
        }
        info(subjectTrigger, failedValidationMsg)
        return null
    }

}