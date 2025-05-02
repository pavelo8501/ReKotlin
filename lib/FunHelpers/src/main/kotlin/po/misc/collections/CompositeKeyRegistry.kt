package po.misc.collections


fun <SO : Identifiable, E : Enum<E>> SO.registerKey(parameter: E): CompositeKey<SO, E> {
    return CompositeKeyRegistry.getKey(this, parameter)
}

object CompositeKeyRegistry {
    private val registry = mutableMapOf<Pair<String, Enum<*>>, CompositeKey<*, *>>()

    @Suppress("UNCHECKED_CAST")
    fun <SO : Identifiable, E : Enum<E>> getKey(sourceObject: SO, parameter: E): CompositeKey<SO, E> {
        val key = Pair(sourceObject.qualifiedName, parameter)
        return registry.getOrPut(key) {
            CompositeKey(sourceObject, parameter)
        } as CompositeKey<SO, E>
    }
}