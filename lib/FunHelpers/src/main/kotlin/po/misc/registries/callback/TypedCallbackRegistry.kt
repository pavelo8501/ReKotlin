package po.misc.registries.callback

import po.misc.collections.CompositeKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased


abstract class TypedCallback<T, R>(){
    abstract val callback : (T)-> R
}

open class TypedCallbackRegistry<T, R>(){

    class Callback<T, R>(override val callback : (T)-> R):TypedCallback<T, R>()

    private val subscriptions = mutableMapOf<CompositeKey, TypedCallback<T, R>>()

    val triggeredHistrory = mutableMapOf<ValueBased, T>()

    fun subscribe(component: Identifiable, type: ValueBased,  callback : (T)-> R) {
        val key = CompositeKey(component, type)
        subscriptions[key] = Callback(callback)
        val missedEvent = triggeredHistrory[type]
        if(missedEvent != null){
            callback.invoke(missedEvent)
            triggeredHistrory.remove(type)
        }
    }

    fun trigger(type: ValueBased, value:T) {
        val toCall  = subscriptions.filter { it.key.type.value == type.value }
        triggeredHistrory[type] = value
        toCall.values.forEach {typedCallback->
            typedCallback.callback.invoke(value)
        }
    }

    fun trigger(component: Identifiable, type: ValueBased, value:T) {
        val key = CompositeKey(component, type)
        subscriptions[key]?.callback?.invoke(value)
    }

    fun clear(component: Identifiable? = null, type: ValueBased? = null) {
        when {
            component != null && type != null -> subscriptions.remove(CompositeKey(component, type))
            component != null -> subscriptions.keys.removeIf { it.component == component }
            type != null -> subscriptions.keys.removeIf { it.type == type }
            else -> subscriptions.clear()
        }
    }
}