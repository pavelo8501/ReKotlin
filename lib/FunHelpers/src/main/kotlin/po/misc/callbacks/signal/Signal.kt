package po.misc.callbacks.signal

import po.misc.callbacks.common.ListenerResult
import po.misc.collections.lambda_map.LambdaMap
import po.misc.collections.lambda_map.toCallable
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.context.component.ComponentID
import po.misc.data.logging.NotificationTopic
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.ClassInfo
import po.misc.exceptions.handling.Suspended
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.token.TypeToken
import kotlin.collections.component1
import kotlin.collections.component2


sealed interface SignalBuilder<T: Any, R: Any>{

    private val signal: Signal<T, R> get() = this as Signal
    fun signalName(name: String):ComponentID{
        val id =  ComponentID(name, signal)
        id.classInfo.genericInfoBacking.addAll(signal.componentID.classInfo.genericInfoBacking)
        signal.componentID = id
        return id
    }
    fun onSignal(callback: (T)-> R)
    fun onSignal(listener: TraceableContext, callback: (T) -> R)
    fun onSignal(suspended: Suspended, callback: suspend (T)-> R)
    fun onSignal(listener: TraceableContext, suspended: Suspended, callback: suspend (T) -> R)

}



class Signal<T: Any, R : Any>(
   val paramType: TypeToken<T>,
   val resultType: TypeToken<R>
): SignalBuilder<T, R>, Component {

    private val subjectReg : String = "Event initialized"
    private val subjectKey = "Key overwritten"

    private val messageReg : (String, TraceableContext)-> String = {callbackType, context->
        if(context === this){
            "$callbackType initialized"
        }else{
            "$callbackType initialized for ${ClassResolver.instanceName(context)}"
        }
    }

    private val messageKey: (Any) -> String = {
        "$subjectKey for Class: ${it::class.simpleOrAnon} with HashCode:  ${it.hashCode()}"
    }

    internal val listeners: LambdaMap<T, R> = LambdaMap()

    internal val classInfo: ClassInfo = ClassResolver.classInfo(this)
            .addParamInfo("T", paramType)
                .addParamInfo("R", resultType)

    override var componentID: ComponentID = ComponentID("Signal",  classInfo)


    init {
        listeners.onKeyOverwritten = {
            warn(subjectKey, messageKey(it))
        }
    }

    val signal : Boolean get() = listeners.values.isEmpty() &&  listeners.values.any {
        it.suspended == null
    }

    val signalSuspended: Boolean get() = listeners.values.isEmpty() &&  listeners.values.any {
        it.suspended == Suspended
    }

    override fun onSignal(callback: (T) -> R) {
        notify(NotificationTopic.Debug, subjectReg, messageReg("Lambda", this))
        listeners[this] = callback.toCallable()
    }
    override fun onSignal(listener: TraceableContext, callback: (T) -> R) {
        notify(NotificationTopic.Debug, subjectReg, messageReg("Lambda", listener))
        listeners[listener] = callback.toCallable()
    }
    override fun onSignal(suspended: Suspended, callback: suspend (T) -> R) {
        notify(NotificationTopic.Debug, subjectReg, messageReg("Suspending lambda", this))
        listeners[this] = toCallable(callback)
    }
    override fun onSignal(listener: TraceableContext, suspended: Suspended, callback: suspend (T) -> R) {
        notify(NotificationTopic.Debug, subjectReg, messageReg("Suspending lambda", listener))
        listeners[listener] = toCallable(callback)
    }

    fun trigger(value: T): List<ListenerResult<R>> {
        return listeners.map { (listener, callable) ->
            val result = callable.invoke(value)
            ListenerResult(listener, result)
        }
    }
    fun trigger(forReceiver: TraceableContext, value: T): R? {
        return listeners[forReceiver]?.invoke(value)
    }

    suspend fun trigger(value: T, suspended: Suspended): List<ListenerResult<R>> {
        return listeners.map { (listener, callable) ->
            val result = callable.invoke(value, suspended)
            ListenerResult(listener, result)
        }
    }
    suspend fun trigger(forReceiver: TraceableContext, value: T, suspended: Suspended): R? {
        return listeners[forReceiver]?.invoke(value, suspended)
    }
}