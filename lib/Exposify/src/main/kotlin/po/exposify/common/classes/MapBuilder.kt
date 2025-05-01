package po.exposify.common.classes



inline fun <reified K, reified T> repoBuilder(key:K, item : T, initial :  Map<K, T> = emptyMap<K,T>()):Map<K, T>{
    val map: MutableMap<K, T> = initial.toMutableMap()
    map[key] = item
    return map
}



/**
 * Represents a builder for creating immutable lists dynamically.
 *
 * This class provides a fluent API to add elements conditionally or in bulk.
 *
 * @param T The type of elements in the list.
 */
open class MapBuilder<K, T>(initial: Map<K, T> = emptyMap()) where K : Any, T: Any {
    private val _map: MutableMap<K, T> = initial.toMutableMap()

    val map: Map<K, T> get() = _map
    fun add(key: K, element: T): Map<K, T> {
        apply {
            _map[key] = element
            val thisMap =  this.map
            thisMap
        }
        return map
    }

    fun put(key: K, element: T): MapBuilder<K, T> {
        apply {
            _map[key] = element
        }
        return this
    }

    fun putIfAbsent(key: K, element: T): MapBuilder<K, T> {
        if (!_map.containsKey(key)) {
            put(key, element)
        }
        return this
    }

    fun addAll( list  : List<Pair<K, T>>){
        list.forEach {
            put(it.first, it.second)
        }
    }

    fun addAll(vararg keyValuePairs : Pair<K, T>){
        addAll(keyValuePairs.toList())
    }

    fun addIf(key: K,  element: T, predicate: (T, List<T>) -> Boolean) = apply {
        if (predicate(element, _map.values.toList())) _map[key] = element
    }

    fun <R> addTransformed(key: K,  element: R, transform: (R) -> T) = apply {
        _map[key] = transform(element)
    }

    fun clear() = apply { _map.clear() }

    fun build(): Map<K, T> = map
}