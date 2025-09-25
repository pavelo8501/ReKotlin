package po.misc.callbacks.events

import po.misc.context.CTX
import po.misc.types.TypeData
import po.misc.types.helpers.simpleOrNan
import java.util.concurrent.atomic.AtomicLong


@PublishedApi
internal class ListenerKey(
    val id: Long,
    val type: TypeData<*>
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

    val activeListeners: Int get() = listenerMap.size

    inline fun <reified L : Any> onEventTriggered(listener: L, noinline onTriggered: (T) -> Unit) {
        val key = when (listener) {
            is CTX -> {
                val identity = listener.identity
                ListenerKey(identity.numericId, identity.typeData).provideName(identity.identifiedByName)
            }

            else -> {
                ListenerKey(nextId.incrementAndGet(), TypeData.create<L>()).provideName(listener::class.simpleOrNan())
            }
        }
        listenerMap[key] = onTriggered
    }

    inline fun <reified T: Any> isListenedBy(): Boolean {
        val type = TypeData.create<T>()
        val found = listenerMap.keys.toList().firstNotNullOfOrNull { it.type == type }
        return found != null
    }


    fun isListenedBy(typeData: TypeData<*>): Boolean {
        val found = listenerMap.keys.toList().firstNotNullOfOrNull { it.type == typeData }
        return found != null
    }

    internal fun notifyTriggered(value: T) {
        listenerMap.values.forEach {
            it.invoke(value)
        }
    }

    fun CallbackEventBase<*, T, *>.dropListeners() {
        listenerMap.clear()
    }
}

