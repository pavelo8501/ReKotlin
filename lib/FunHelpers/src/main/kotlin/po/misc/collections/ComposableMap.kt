package po.misc.collections



class ComposableMap<K: Any, V: Any>(): AbstractMutableMap<K, V>(){

    @PublishedApi
    internal val mapBacking: MutableMap<K, V> = mutableMapOf()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = mapBacking.entries

    override fun put(key: K, value:V): V? {
        return  mapBacking.put(key, value)
    }

}