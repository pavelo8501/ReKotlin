package po.misc.callbacks.events

import po.misc.exceptions.ManagedException
import po.misc.context.TraceableContext
import po.misc.types.TypeData
import kotlin.Boolean


/**
 * Builder API for registering callbacks on events.
 *
 * @param H the type of the host that owns the event (e.g. a UI component or context).
 * @param T the type of data emitted with the event (must be [EventData] for unhosted events).
 * @param R the return type of the callback (commonly `Unit`).
 *
 * Provides two registration methods:
 * - [onEvent] for synchronous callbacks
 * - [onEventSuspending] for suspending callbacks
 *
 * Example:
 * ```
 * eventOf<ClickData> {
 *     onEvent { println("Clicked at ${it.x}, ${it.y}") }
 *     onEventSuspending { delay(100); println("Async click handled") }
 * }
 * ```
 */
sealed interface HostedEventBuilder<H, T, R>  where H: EventHost{

    fun onEvent(callback: H.(T)-> R)
    fun onEventSuspending(callback: suspend H.(T)-> R)
}


sealed interface EventBuilder<T, R>{
    fun onEvent(callback: (T)-> R)
    fun onEventSuspending(callback: suspend (T)-> R)
}


sealed class CallbackEventBase<H, T, R>(
    val  paramType: TypeData<T>
): TraceableContext where H: Any, T: Any  {


    internal var result:R? = null
    fun getLastResult():R?{
        return  result
    }

    abstract val listeners : EventListeners<T>
    protected val validators: MutableList<ValidatableEvent> = mutableListOf()

    internal open var onEventCallback: ((T)-> R)? = null

    val event: Boolean get() = onEventCallback  != null

    internal var onEventSuspendedCallback: (suspend (T)-> R)? = null
    val eventSuspended: Boolean get() = onEventSuspendedCallback  != null


    protected fun isValid(value: Validatable,  throws: Boolean): Boolean =
        validators.all { it.validate(value, throws) }

    protected fun throwIfValidatorsEmpty(){
        if(validators.isEmpty()){
            throw ManagedException(this, "Validation will always fail. No Validators present")
        }
    }

    /**
     * Triggers all synchronous event callbacks with the given [value],
     * validating against [validator] if provided.
     *
     * @param value the event data to dispatch.
     * @param validator a validation context to check before firing.
     * @param validationThrows if `true`, throws an exception when no validators are present
     *                         or validation fails. If `false`, silently skips triggering.
     * @return `true` if any callback was triggered, `false` otherwise.
     */
    fun trigger(value: T, notifyListeners: Boolean = true): Boolean{
        var triggered = false
        onEventCallback?.let {
            result = it.invoke(value)
            triggered = true
        }
        if(notifyListeners){
            listeners.notifyTriggered(value)
        }
        return triggered
    }

    fun trigger(
        value: T,
        validator: Validatable,
        validationThrows: Boolean = false,
        notifyListeners: Boolean = true,
    ): Boolean{
        var triggered = false
        if(validationThrows){
            throwIfValidatorsEmpty()
        }
        if(isValid(validator, validationThrows)){
            triggered = trigger(value, notifyListeners)
        }
        return triggered
    }

    /**
     * Triggers all suspending event callbacks with the given [value],
     * validating against [validator] if provided.
     *
     * @param value the event data to dispatch.
     * @param validator a validation context to check before firing.
     * @param validationThrows if `true`, throws an exception when no validators are present
     *                         or validation fails. If `false`, silently skips triggering.
     * @return `true` if any callback was triggered, `false` otherwise.
     */
    suspend fun triggerSuspending(value: T): Boolean{
        var triggered = false
        onEventSuspendedCallback?.let {
            result = it.invoke(value)
            triggered = true
        }
        listeners.notifyTriggered(value, true)
        return triggered
    }


    suspend fun triggerSuspending(
        value: T, validator: Validatable,
        validationThrows: Boolean =  false,
    ):Boolean{
        var triggered = false
        if(validationThrows){
            throwIfValidatorsEmpty()
        }
        if(isValid(validator, validationThrows)){
            triggered = triggerSuspending(value)
        }
        return triggered
    }


    /**
     * Triggers both synchronous and suspending event callbacks with the given [value].
     *
     * If [validator] is provided:
     * - Validation is performed independently for each pipeline (sync and suspend).
     * - If validation fails:
     *   - When [validationThrows] is `true` (default inside [trigger]/[triggerSuspending]),
     *     an exception is thrown.
     *   - When `false`, the pipeline simply skips execution.
     *
     * Execution order:
     * 1. Synchronous callbacks ([trigger]) are fired first.
     * 2. Suspending callbacks ([triggerSuspending]) are fired next.
     *
     * If [validator] is `null`, both pipelines run without validation.
     *
     * @param value the event data to dispatch to handlers.
     * @param validator optional validator applied to each pipeline.
     *
     * @see trigger
     * @see triggerSuspending
     */
    suspend fun triggerBoth(value: T, validator: Validatable?) {
        if(validator != null){
            trigger(value, validator, validationThrows = true, notifyListeners =  false)
            triggerSuspending(value, validator, validationThrows = true)
        }else{
            trigger(value, notifyListeners =  false)
            triggerSuspending(value)
        }
    }

    /**
     * Adds a validator that will be consulted before triggering events.
     *
     * All registered validators must pass (or at least one, depending on [isValid] logic)
     * in order for the event to be dispatched.
     *
     * @param validator the validation rule to add.
     */
    fun addValidator(validator: ValidatableEvent) {
        validators.add(validator)
    }

}


open class CallbackEvent<T: Any, R>(
    paramType: TypeData<T>
): CallbackEventBase<Unit, T, R>(paramType),EventBuilder<T, R>{
    override val  listeners : EventListeners<T> by lazy { EventListeners() }
    override fun onEvent(callback: (T)-> R){
        onEventCallback = callback
    }
    override fun onEventSuspending(callback: suspend (T)-> R){
        onEventSuspendedCallback = callback
    }

}

open class ParametrizedEvent<H, T, R>(
    val  host: H,
    paramType: TypeData<T>
): CallbackEventBase<H, T, R>(paramType), HostedEventBuilder<H, T, R> where H: EventHost, T: Any {

    override val  listeners : EventListeners<T> by lazy {  EventListeners() }


    override fun onEvent(callback: H.(T) -> R) {
        super.onEventCallback = {param->
            callback.invoke(host, param)
        }
    }

    override fun onEventSuspending(callback: suspend H.(T) -> R){
        super.onEventSuspendedCallback = {param->
            callback.invoke(host, param)
        }
    }

}

