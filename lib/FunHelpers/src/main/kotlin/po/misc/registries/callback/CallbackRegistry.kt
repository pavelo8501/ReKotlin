package po.misc.registries.callback

import po.misc.collections.CompositeKey
import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.interfaces.ValueBased



class CallbackRegistry {

    private val subscriptions = mutableMapOf<CompositeKey, () -> Unit>()
    fun subscribe(component: CTX, type: ValueBased, callback: () -> Unit) {
        val key = CompositeKey(component, type)
        subscriptions[key] = callback
    }

    fun trigger(component: CTX, type: ValueBased) {
        val key = CompositeKey(component, type)
        subscriptions[key]?.invoke()
    }

    fun trigger(type: ValueBased) {
        val toCall  = subscriptions.filter { it.key.type == type }
        toCall.values.forEach {callback->
            callback.invoke()
        }
    }

    fun clear(component: CTX? = null, type: ValueBased? = null) {
        when {
            component != null && type != null -> subscriptions.remove(CompositeKey(component, type))
            component != null -> subscriptions.keys.removeIf { it.component == component }
            type != null -> subscriptions.keys.removeIf { it.type == type }
            else -> subscriptions.clear()
        }
    }
}


