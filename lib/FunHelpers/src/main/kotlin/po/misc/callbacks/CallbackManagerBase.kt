package po.misc.callbacks

import po.misc.callbacks.models.ManagerStats
import po.misc.callbacks.models.PostTriggerEvent
import po.misc.callbacks.models.SubscriptionRecord
import po.misc.callbacks.models.TriggerEvent
import po.misc.collections.CompositeKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import kotlin.collections.set


@JvmInline
value class ValueBasedImpl(override val value: Int) : ValueBased

fun switchToValueClass(valueBasedInterface:  ValueBased):ValueBasedImpl{
   return ValueBasedImpl(valueBasedInterface.value)
}

abstract class CallbackManagerBase<T: Any, R: Any> {

    abstract val ownName: String

     protected open val subscriptions : MutableMap<CompositeKey, CallbackPayloadBase<T, R>> = mutableMapOf()

    var onNewSubscription: ((ManagerStats)-> Unit)? = null
    var onBeforeTrigger: ((TriggerEvent<T>)-> Unit)? = null
    var onAfterTriggered: ((PostTriggerEvent)-> Unit)? = null

    val temporaryUntriggeredData = mutableMapOf<Int, T>()

    abstract fun  subscribe(identifiable: Identifiable, callbackPayload: CallbackPayloadBase<T,R>)
    abstract fun trigger(identifiable: Identifiable, type: ValueBased, value: T):R?

    protected fun generateKey(id: Identifiable, type: ValueBased):CompositeKey{

      return  CompositeKey(id, switchToValueClass(type))
    }

    fun <T: Any, R: Any>  CallbackManagerBase<T, R>.createStats(
        identity: Identifiable,
        payload: CallbackPayloadBase<T,R>):ManagerStats{

        return ManagerStats(SubscriptionRecord(identity, payload.event), this.subscriptions.size, ownName)
    }

    fun <T: Any>  CallbackManagerBase<T, *>.createTriggerEvent(id: Identifiable, type: ValueBased, value:T): TriggerEvent<T>{
        return TriggerEvent(id, switchToValueClass(type), value, ownName)
    }

   fun <P2 : CallbackPayloadBase<T, R>> transferTo(
        targetManager: CallbackManagerBase<T, R>,
        types: List<ValueBased>,
        targetPayloadFunction: (CallbackPayloadBase<T,R>) -> P2
        ) {
       types.forEach { type ->
           val filtered = subscriptions.filter { (_, value) -> value.event.value == type.value }
           filtered.forEach { (key, payload) ->
               targetManager.subscribe(key.component, targetPayloadFunction(payload))
           }
       }
   }
    fun clear(){
        subscriptions.clear()
    }
}


class CallbackManager<T: Any, R: Any>(): CallbackManagerBase<T, R>(){

    override val ownName: String
        get() = this::class.simpleName.toString()


    override fun subscribe(identifiable: Identifiable, callbackPayload: CallbackPayloadBase<T, R>) {
        subscriptions[generateKey(identifiable,callbackPayload.event)] = callbackPayload
        onNewSubscription?.invoke(this@CallbackManager.createStats(identifiable, callbackPayload))
        temporaryUntriggeredData[callbackPayload.event.value]?.let {
            callbackPayload.trigger(it)
        }
    }

    fun subscribe(identifiable: Identifiable, event: ValueBased, callback: (T)-> R) {
        val callbackPayload = ResultCallbackPayload.create<T, R>(event, callback)
        subscriptions[generateKey(identifiable,callbackPayload.event)] = callbackPayload
        onNewSubscription?.invoke(this@CallbackManager.createStats(identifiable, callbackPayload))
        temporaryUntriggeredData[callbackPayload.event.value]?.let {
            callbackPayload.trigger(it)
        }
    }

    override fun trigger(identifiable: Identifiable, type: ValueBased, value: T): R? {
        val subscription  = subscriptions[generateKey(identifiable, type)]
        return subscription?.trigger(value) ?:run {
            temporaryUntriggeredData[type.value] = value
            null
        }
    }

    fun triggerForAll(type: ValueBased, value: T) {
        var triggersCount = 0
        subscriptions.filter { (key, value) -> switchToValueClass(key.type)  == switchToValueClass(type)}
            .forEach { entry ->
                triggersCount++
                onBeforeTrigger?.invoke(createTriggerEvent(entry.key.component, entry.key.type, value))
                entry.value.callback.invoke(value)
            }

        if(triggersCount == 0){
            temporaryUntriggeredData[type.value] = value
        }

        onAfterTriggered?.invoke(PostTriggerEvent(triggersCount, subscriptions.size, ownName))
    }
}

