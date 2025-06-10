package po.misc.callbacks

import po.misc.callbacks.models.ManagerStats
import po.misc.callbacks.models.PostTriggerEvent
import po.misc.callbacks.models.SubscriptionRecord
import po.misc.callbacks.models.TriggerEvent
import po.misc.collections.CompositeKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

abstract class CallbackManagerBase<T: Any, R: Any, P: CallbackPayloadBase<T, R>> {

    abstract val ownName: String

    protected  val subscriptions : MutableMap<CompositeKey, P> = mutableMapOf()

    var onNewSubscription: ((ManagerStats)-> Unit)? = null
    var onBeforeTrigger: ((TriggerEvent<T>)-> Unit)? = null
    var onAfterTriggered: ((PostTriggerEvent)-> Unit)? = null

    abstract fun subscribe(identifiable: Identifiable, callbackPayload: P)
    abstract fun trigger(identifiable: Identifiable, type: ValueBased, value: T):R?

    protected fun generateKey(id: Identifiable, type: ValueBased):CompositeKey{
      return  CompositeKey(id, type)
    }

    fun <T: Any, R: Any, P: CallbackPayloadBase<T, R>>  CallbackManagerBase<T, R, P>.createStats(identity: Identifiable, payload: P):ManagerStats{
        return ManagerStats(SubscriptionRecord(identity, payload.event), this.subscriptions.size, ownName)
    }

    fun <T: Any>  CallbackManagerBase<T, *, *>.createTriggerEvent(
      id: Identifiable,
      type: ValueBased,
      value:T
    ): TriggerEvent<T>{
        return TriggerEvent(id, type, value, ownName)
    }

   fun <P2 : CallbackPayloadBase<T, R>> transferTo(
        targetManager: CallbackManagerBase<T, R, P2>,
        types: List<ValueBased>,
        targetPayloadFunction: (P) -> P2
        ) {

        types.forEach {type->
            val filtered = subscriptions.filter { (_, value)->  value.event.value ==  type.value }
            filtered.forEach { (key, payload)->
                targetManager.subscribe(key.component,  targetPayloadFunction(payload))
            }

        }
    }

    fun clear(){
        subscriptions.clear()
    }

}


class CallbackManager<T : Any, R: Any, P>(): CallbackManagerBase<T, R, P>()  where  P: CallbackPayloadBase<T, R> {

    override val ownName: String
        get() = this::class.simpleName.toString()

    override fun subscribe(identifiable: Identifiable, callbackPayload: P) {


        subscriptions[generateKey(identifiable,callbackPayload.event)] = callbackPayload
        onNewSubscription?.invoke(this.createStats(identifiable, callbackPayload))
    }

    override fun trigger(identifiable: Identifiable, type: ValueBased, value: T): R? {
        return subscriptions[generateKey(identifiable, type)]?.trigger(value)
    }

    fun triggerForAll(type: ValueBased, value: T) {
        var triggersCount = 0
        subscriptions.filter { keyValue -> keyValue.value.event == type }
            .forEach { entry ->
                triggersCount++
                onBeforeTrigger?.invoke(createTriggerEvent(entry.key.component, entry.value.event, value))
                entry.value.callback.invoke(value)
            }
        onAfterTriggered?.invoke(PostTriggerEvent(triggersCount, subscriptions.size, ownName))
    }

}


//open class TypedCallbackRegistry<T, R>(){
//
//    class Callback<T, R>(override val callback : (T)-> R):TypedCallback<T, R>()
//    private val subscriptions = mutableMapOf<CompositeKey, TypedCallback<T, R>>()
//    val unTriggered= mutableMapOf<ValueBased, T>()
//
//    var onNewSubscription: ((ValueBased, Identifiable)-> Unit)? = null
//    var onKeyOverwrite: ((key: String, Identifiable)-> Unit)? = null
//    var onBeforeTrigger: ((ValueBased, Identifiable, T)-> Unit)? = null
//    var onAfterTriggered: ((triggerCount:Int)-> Unit)? = null
//
//    fun subscribe(component: Identifiable, type: ValueBased,  callback : (T)-> R) {
//
//        onNewSubscription?.invoke(type, component)
//
//        val key = CompositeKey(component, type)
//        if(subscriptions.contains(key)){
//            onKeyOverwrite?.invoke(key.toString(), component)
//        }
//        subscriptions[key] = Callback(callback)
//
//        val missedEvent = unTriggered[type]
//
//        if(missedEvent != null){
//            callback.invoke(missedEvent)
//            unTriggered.remove(type)
//        }
//    }
//
//    fun triggerForAll(type: ValueBased, value:T) {
//        var triggersCount = 0
//        subscriptions.filter { keyValue -> keyValue.key.type.value == type.value }
//            .forEach {entry->
//                triggersCount++
//                onBeforeTrigger?.invoke(type, entry.key.component, value)
//                entry.value.callback.invoke(value)
//            }
//        if(triggersCount == 0){
//            unTriggered[type] = value
//        }
//        onAfterTriggered?.invoke(triggersCount)
//    }
//
//    fun trigger(component: Identifiable, type: ValueBased, value:T) {
//        val key = CompositeKey(component, type)
//        subscriptions[key]?.let { callbackContainer->
//            onBeforeTrigger?.invoke(key.type, key.component, value)
//            callbackContainer.callback.invoke(value)
//            onAfterTriggered?.invoke(1)
//        }?:run {
//            unTriggered[type] = value
//            onAfterTriggered?.invoke(0)
//        }
//    }
//
//    fun hasSubscribersFor(type: ValueBased): Boolean =
//        subscriptions.keys.any { it.type == type }
//
//    fun clear(component: Identifiable? = null, type: ValueBased? = null) {
//        when {
//            component != null && type != null -> subscriptions.remove(CompositeKey(component, type))
//            component != null -> subscriptions.keys.removeIf { it.component == component }
//            type != null -> subscriptions.keys.removeIf { it.type == type }
//            else -> subscriptions.clear()
//        }
//    }
//}