package po.misc.registries.callback

import po.misc.collections.CompositeKey
import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.interfaces.ValueBased


abstract class TypedCallback<T, R>(){
    abstract val callback : (T)-> R
}

open class TypedCallbackRegistry<T, R>(){

    class Callback<T, R>(override val callback : (T)-> R):TypedCallback<T, R>()
    private val subscriptions = mutableMapOf<CompositeKey, TypedCallback<T, R>>()
    val unTriggered= mutableMapOf<ValueBased, T>()

    var onNewSubscription: ((ValueBased, CTX)-> Unit)? = null
    var onKeyOverwrite: ((key: String, CTX)-> Unit)? = null
    var onBeforeTrigger: ((ValueBased, CTX, T)-> Unit)? = null
    var onAfterTriggered: ((triggerCount:Int)-> Unit)? = null

    fun subscribe(component: CTX, type: ValueBased,  callback : (T)-> R) {

        onNewSubscription?.invoke(type, component)

        val key = CompositeKey(component, type)
        if(subscriptions.contains(key)){
            onKeyOverwrite?.invoke(key.toString(), component)
        }
        subscriptions[key] = Callback(callback)

        val missedEvent = unTriggered[type]

        if(missedEvent != null){
            callback.invoke(missedEvent)
            unTriggered.remove(type)
        }
    }

    fun triggerForAll(type: ValueBased, value:T) {
        var triggersCount = 0
        subscriptions.filter { keyValue -> keyValue.key.type.value == type.value }
            .forEach {entry->
                triggersCount++
                onBeforeTrigger?.invoke(type, entry.key.component, value)
                entry.value.callback.invoke(value)
            }
        if(triggersCount == 0){
            unTriggered[type] = value
        }
        onAfterTriggered?.invoke(triggersCount)
    }

    fun trigger(component: CTX, type: ValueBased, value:T) {
        val key = CompositeKey(component, type)
        subscriptions[key]?.let { callbackContainer->
            onBeforeTrigger?.invoke(key.type, key.component, value)
            callbackContainer.callback.invoke(value)
            onAfterTriggered?.invoke(1)
        }?:run {
            unTriggered[type] = value
            onAfterTriggered?.invoke(0)
        }
    }

    fun hasSubscribersFor(type: ValueBased): Boolean =
        subscriptions.keys.any { it.type == type }

    fun clear(component: CTX? = null, type: ValueBased? = null) {
        when {
            component != null && type != null -> subscriptions.remove(CompositeKey(component, type))
            component != null -> subscriptions.keys.removeIf { it.component == component }
            type != null -> subscriptions.keys.removeIf { it.type == type }
            else -> subscriptions.clear()
        }
    }
}