package po.misc.collections

import po.misc.context.CTX
import po.misc.context.Identifiable



object CompositeKeyRegistry {
    private val registry = mutableMapOf<Pair<String, Enum<*>>, CompositeEnumKey<*>>()

    @Suppress("UNCHECKED_CAST")
    fun <E : Enum<E>> getKey(sourceObject: CTX, parameter: E): CompositeEnumKey<E> {
        val key = Pair(sourceObject.completeName, parameter)
        return registry.getOrPut(key) {
            CompositeEnumKey(parameter, sourceObject)
        } as CompositeEnumKey<E>
    }
}