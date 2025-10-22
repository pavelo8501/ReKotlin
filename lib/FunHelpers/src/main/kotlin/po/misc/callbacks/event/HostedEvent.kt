package po.misc.callbacks.event

import po.misc.callbacks.common.EventHost
import po.misc.callbacks.common.EventLogRecord
import po.misc.callbacks.common.ListenerResult
import po.misc.callbacks.validator.ReactiveValidator
import po.misc.collections.lambda_map.LambdaMap
import po.misc.collections.lambda_map.toCallable
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.LogProvider
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.processor.logProcessor
import po.misc.debugging.ClassResolver
import po.misc.exceptions.handling.Suspended
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.token.TypeToken

sealed interface HostedEventBuilder<H : EventHost, T: Any, R: Any> {
    private val thisAsEvent  get() = this as HostedEvent<H, T, R>

    fun onEvent(callback: H.(T) -> R)
    fun onEvent(listener: TraceableContext, callback: H.(T) -> R)
    fun onEvent(suspended: Suspended, callback: suspend H.(T) -> R)
    fun onEvent(listener: TraceableContext, suspended: Suspended, callback: suspend H.(T) -> R)

    fun withValidation(predicate: (T)-> Boolean):ReactiveValidator<T>{
        return with(thisAsEvent){
            registerValidator(ReactiveValidator(predicate))
        }
    }
}

class HostedEvent<H: EventHost, T : Any, R: Any>(
    internal val host: H,
    val paramType: TypeToken<T>,
    val resultType: TypeToken<R>
): HostedEventBuilder<H, T, R>, Component, LogProvider<EventLogRecord> {

    private val messageReg : (String, TraceableContext)-> String = {callbackType, context->
        if(context === this){
            "$callbackType initialized"
        }else{
            "$callbackType initialized for ${ClassResolver.instanceName(context)}"
        }
    }

    internal var validator: ReactiveValidator<T>? = null

    internal val listeners: LambdaMap<T, R> = LambdaMap()

    override var componentID: ComponentID = componentID("HostedEvent")
        .addParamInfo("T",paramType)
            .addParamInfo("R", resultType)

    internal val processor = logProcessor()


    init {
        listeners.onKeyOverwritten = {
            warn(subjectKey, messageKey(it))
        }
    }

    fun registerValidator(validator: ReactiveValidator<T>): ReactiveValidator<T>{
        this.validator = validator
        return validator
    }

    override fun onEvent(callback: H.(T) -> R){
        notify(NotificationTopic.Debug, subjectReg, messageReg("Lambda", this))
        listeners[this] = callback.toCallable(host)
    }
    override fun onEvent(listener: TraceableContext, callback: H.(T) -> R) {
        notify(NotificationTopic.Debug, subjectReg, messageReg("Lambda", listener))
        listeners[listener] = callback.toCallable(host)
    }

    override fun onEvent(suspended: Suspended, callback: suspend H.(T) -> R) {
        notify(NotificationTopic.Debug, subjectReg, messageReg("Suspending lambda", this))
        listeners[this] = host.toCallable(callback)
    }
    override fun onEvent(listener: TraceableContext, suspended: Suspended, callback: suspend H.(T) -> R) {
        notify(NotificationTopic.Debug, subjectReg, messageReg("Suspending lambda", listener))
        listeners[listener] = host.toCallable(callback)
    }

    fun trigger(value: T): List<ListenerResult<R>> {
        return listeners.map { (listener, callable) ->
            val result = callable.invoke(value)
            debug(subjectInvoke, messageInvoke(listener))
            ListenerResult(listener, result)
        }
    }
    fun trigger(forListener: TraceableContext, value: T): R? {
        val result = listeners[forListener]?.invoke(value)
        debug(subjectInvoke, messageInvoke(forListener))
        return result
    }
    suspend fun trigger(value: T, suspended: Suspended): List<ListenerResult<R>> {
        return listeners.map { (listener, callable) ->
            val result = callable.invoke(value, suspended)
            debug(subjectInvoke, messageInvoke(listener))
            ListenerResult(listener, result)
        }
    }
    suspend fun trigger(forListener: TraceableContext, value: T, suspended: Suspended): R? {
        val result = listeners[forListener]?.invoke(value, suspended)
        debug(subjectInvoke, messageInvoke(forListener))
        return result
    }

    fun triggerValidating(value: T):List<ListenerResult<R>>{
        val activeValidator = validator
        if(activeValidator != null){
            if(activeValidator.validate(value)){
               return trigger(value)
            }
        }
        return emptyList()
    }
    fun triggerValidating(forListener: TraceableContext, value: T):R?{
        val activeValidator = validator
        if(activeValidator != null){
            if(activeValidator.validate(value)){
                return trigger(forListener, value)
            }
        }
        return null
    }
    suspend fun triggerValidating(value: T, suspended: Suspended):List<ListenerResult<R>>{
        val activeValidator = validator
        if(activeValidator != null){
            if(activeValidator.validate(value, suspended)){
                return trigger(value, suspended)
            }
        }
        return emptyList()
    }
    suspend fun triggerValidating(forListener: TraceableContext, value: T, suspended: Suspended):R?{
        val activeValidator = validator
        if(activeValidator != null){
            if(activeValidator.validate(value, suspended)){
                return trigger(forListener, value, suspended)
            }
        }
        return null
    }


    companion object{

        private val subjectReg : String = "Event initialized"
        private val subjectKey = "Key overwritten"
        private val subjectInvoke : String = "Lambda Invoked"
        private val subjectValidation : String = "Validating"


        private val validationError: (String) -> String = { "Validation failed. Reason: $it" }

        private val messageInvoke: (Any) -> String = {
            "$subjectInvoke for Class: ${it::class.simpleOrAnon} with HashCode: ${it.hashCode()}"
        }

        private val messageKey: (Any) -> String = {
            "$subjectKey for Class: ${it::class.simpleOrAnon} with HashCode:  ${it.hashCode()}"
        }
    }
}
