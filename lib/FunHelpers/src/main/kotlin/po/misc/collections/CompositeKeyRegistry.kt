package po.misc.collections

import po.misc.interfaces.Identifiable


fun <SO : Identifiable, E : Enum<E>> SO.registerKey(parameter: E): CompositeEnumKey<SO, E> {
    return CompositeKeyRegistry.getKey(this, parameter)
}

object CompositeKeyRegistry {
    private val registry = mutableMapOf<Pair<String, Enum<*>>, CompositeEnumKey<*, *>>()

    @Suppress("UNCHECKED_CAST")
    fun <SO : Identifiable, E : Enum<E>> getKey(sourceObject: SO, parameter: E): CompositeEnumKey<SO, E> {
        val key = Pair(sourceObject.qualifiedName, parameter)
        return registry.getOrPut(key) {
            CompositeEnumKey(sourceObject, parameter)
        } as CompositeEnumKey<SO, E>
    }
}