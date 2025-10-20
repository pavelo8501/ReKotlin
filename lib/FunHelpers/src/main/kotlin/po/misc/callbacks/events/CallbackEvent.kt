package po.misc.callbacks.events

import po.misc.context.component.Component
import po.misc.exceptions.ManagedException
import po.misc.data.helpers.replaceIfNull
import po.misc.data.logging.Verbosity
import po.misc.exceptions.handling.Suspended
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.token.TypeToken
import kotlin.Boolean


abstract class CallbackEventBase<H : Any, T : Any, R>(
    val  paramType: TypeToken<T>,
    private val hostName: String?,
    private val resultName: String
): Component {

    internal var currentResult:R? = null
    protected val validators: MutableList<ValidatableEvent> = mutableListOf()
    protected val parameterName: String = paramType.typeName

    protected val header: String =  "No header"

    internal open var callback: ((T)-> R)? = null
    internal var suspendedCallback: (suspend (T)-> R?)? = null

    val event: Boolean get() = callback  != null
    val eventSuspended: Boolean get() = suspendedCallback  != null

    abstract val listeners : EventListeners<T>

    fun getLastResult():R?{
        return  currentResult
    }
    protected fun isValid(value: Validatable,  throws: Boolean): Boolean =
        validators.all { it.validate(value, throws) }

    protected fun throwIfValidatorsEmpty(){
        if(validators.isEmpty()){
            throw ManagedException(this, "Validation will always fail. No Validators present")
        }
    }
    protected fun invokeIfAvailable(value: T, warningIfUnavailable: String? = null): Boolean{
        if(!event && warningIfUnavailable != null){
            warn(warningIfUnavailable, "trigger")
        }

        val callback = callback ?: return false
        currentResult = callback(value)
        return true
    }

    protected suspend fun invokeIfAvailable(value: T, suspended: Suspended): Boolean{
        if(!eventSuspended){
            warn("Suspended callback not defined", "trigger(Suspended)")
        }
        val callback = suspendedCallback ?: return false
        currentResult = callback(value)
        return true
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
    fun trigger(value: T): R?{
        if(invokeIfAvailable(value)){
            listeners.notifyTriggered(value)
            return currentResult
        }
        return null
    }

    fun trigger(
        value: T,
        validator: Validatable,
        validationThrows: Boolean = false,
    ): R? {
        if(validationThrows){
            throwIfValidatorsEmpty()
        }
        if(isValid(validator, validationThrows)){
            return trigger(value)
        }
        return null
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
    suspend fun triggerSuspending(value: T): R?{
       if(invokeIfAvailable(value, Suspended)){
           listeners.notifyTriggered(value, true)
           return currentResult
       }else{
           return null
       }
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
            triggered = invokeIfAvailable(value, Suspended)
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
            trigger(value, validator, validationThrows = true)
            triggerSuspending(value, validator, validationThrows = true)
        }else{
            trigger(value)
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
sealed interface HostedEventBuilder<H : EventHost, T: Any, R: Any> {
    private val thisAsEvent  get() = this as HostedEvent<H, T, R>

    fun onEvent(callback: H.(T)-> R)
    fun onEvent(suspended: Suspended, callback: suspend H.(T)-> R?)

    fun withValidation(builder: Validator<H, T, R>.()-> Unit): Validator<H, T, R>{
       val validation =   Validator(thisAsEvent)
       validation.builder()
       thisAsEvent.registerValidator(validation)
       return validation
    }
}

class HostedEvent<H: EventHost, T : Any, R : Any>(
    internal val host: H,
    paramType: TypeToken<T>,
    val resultType: TypeToken<R>
): CallbackEventBase<H, T, R>(paramType, host::class.simpleOrAnon, resultType.typeName), HostedEventBuilder<H, T, R> {

    internal var validator: Validator<H, T, R>? = null

    override val listeners : EventListeners<T> by lazy {  EventListeners() }
    fun registerValidator(eventValidator: Validator<H, T, R>): HostedEvent<H, T, R>{
        validator = eventValidator
        return this
    }

    fun triggerValidating(value: T):R?{
      return  validator?.let {
            info("Trigger validating", "Validator present. Validation")
            val triggerResult = it.validate(value)
            if(it.result?:false){
                info("Trigger validating", "Validation failed")
            }
           triggerResult
        }
    }

    suspend  fun triggerValidating(value: T, suspended: Suspended):R?{
       return validator?.let {
            info("Trigger validating", "Validator present. Validation suspended")
            val triggerResult =it.validate(value, suspended)
            if(it.result?:false){
                info("Trigger validating", "Validation failed")
            }
            triggerResult
        }
    }

    override fun onEvent(callback: H.(T) -> R){
        super.callback = {param->
            callback.invoke(host, param)
        }
    }

    override fun onEvent(suspended: Suspended, callback: suspend H.(T) -> R?){
        super.suspendedCallback = {param->

            callback.invoke(host, param)
        }
    }

    suspend fun trigger(value: T, suspended: Suspended):R?{
        invokeIfAvailable(value)
        return triggerSuspending(value)
    }

}

