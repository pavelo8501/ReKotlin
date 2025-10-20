package po.misc.callbacks.events

import po.misc.context.CTX
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.types.TypeProvider
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.token.TypeToken
import java.util.concurrent.atomic.AtomicLong


@PublishedApi
internal class ListenerKey(
    val id: Long,
    val type: TypeToken<*>
){

    var listenerName: String = ""

    override fun equals(other: Any?): Boolean {
        if(other !is ListenerKey){
            return false
        }
        return other.type == type &&  other.id == id
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    fun provideName(name: String): ListenerKey{
        listenerName = name
        return this
    }

}

class EventListeners<T: Any> {

    @PublishedApi
    internal val nextId = AtomicLong(0)

    @PublishedApi
    internal val listenerMap = mutableMapOf<ListenerKey, (T) -> Unit>()

    @PublishedApi
    internal val suspendableListenerMap = mutableMapOf<ListenerKey, suspend (T) -> Unit>()


    val activeListeners: Int get() = listenerMap.size

    inline fun <reified L : Any> onEventTriggered(listener: L, noinline onTriggered: (T) -> Unit) {

        onEventTriggered(listener, TypeToken.create<L>(),  onTriggered)
    }

    fun onEventTriggered(listener: TypeProvider, onTriggered: (T) -> Unit) {
        when (listener) {
            is CTX -> {
                val identity = listener.identity
                val key = ListenerKey(identity.numericId, identity.typeData).provideName(identity.identifiedByName)
                listenerMap[key] = onTriggered
            }

            else -> {
                listener.types.firstOrNull()?.let {
                    val key = ListenerKey(nextId.incrementAndGet(), it).provideName(listener::class.simpleOrAnon)
                    listenerMap[key] = onTriggered
                }?:run {
                    "TypedObject in onEventTriggered has no type parameters in its types list".output(Colour.Red)
                }
            }
        }
    }

    fun onTriggeredSuspending(listener: TypeProvider, onTriggered: suspend (T) -> Unit) {

        when (listener) {
            is CTX -> {
                val identity = listener.identity
                val key = ListenerKey(identity.numericId, identity.typeData).provideName(identity.identifiedByName)
                suspendableListenerMap[key] = onTriggered
            }
            else -> {
                listener.types.firstOrNull()?.let {
                    val key = ListenerKey(nextId.incrementAndGet(), it).provideName(listener::class.simpleOrAnon)
                    suspendableListenerMap[key] = onTriggered
                }?:run {
                    "TypedObject in onEventTriggered has no type parameters in its types list".output(Colour.Red)
                }
            }
        }
    }

    fun <L : Any> onEventTriggered(listener: L, typeData: TypeToken<L>, onTriggered: (T) -> Unit) {
        val key = when (listener) {
            is CTX -> {
                val identity = listener.identity
                ListenerKey(identity.numericId, identity.typeData).provideName(identity.identifiedByName)
            }
            else -> {
                ListenerKey(nextId.incrementAndGet(), typeData).provideName(listener::class.simpleOrAnon)
            }
        }
        listenerMap[key] = onTriggered
    }

    inline fun <reified T: Any> isListenedBy(): Boolean {
        val type = TypeToken.create<T>()
        val found = listenerMap.keys.toList().firstNotNullOfOrNull { it.type == type }
        return found != null
    }


    fun isListenedBy(typeData: TypeToken<*>): Boolean {
        val found = listenerMap.keys.toList().firstNotNullOfOrNull { it.type == typeData }
        return found != null
    }

    fun notifyTriggered(value: T) {
        listenerMap.values.forEach {
            it.invoke(value)
        }
    }

   suspend fun notifyTriggered(value: T, triggerBoth: Boolean) {
        if(triggerBoth){
            suspendableListenerMap.values.forEach {
                it.invoke(value)
            }
            listenerMap.values.forEach {
                it.invoke(value)
            }
        }else{
            suspendableListenerMap.values.forEach {
                it.invoke(value)
            }
        }
    }

    fun CallbackEventBase<*, T, *>.dropListeners() {
        listenerMap.clear()
    }
}

