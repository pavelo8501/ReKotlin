package po.misc.registries.callback

import po.misc.collections.CompositeKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased


class TypedCallbackRegistry<T: Any> {

    class TypedCallback<T>(
        val callback : (T)-> Unit
    )

    private val subscriptions = mutableMapOf<CompositeKey, TypedCallback<T>>()

    fun subscribe(component: Identifiable, type: ValueBased, callback: (T) -> Unit) {
        val key = CompositeKey(component, type)
        subscriptions[key] = TypedCallback(callback)
    }

    fun trigger(type: ValueBased, value:T) {
        val toCall  = subscriptions.filter { it.key.type.value == type.value }
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