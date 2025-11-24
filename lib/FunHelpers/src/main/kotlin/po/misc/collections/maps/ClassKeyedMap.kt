package po.misc.collections.maps

import po.misc.data.output.output
import po.misc.data.styles.Colour
import kotlin.reflect.KClass




class ClassKeyedMap<K,  V>(): AbstractMutableMap<K, V>()  where K: KClass<out Any>, V:Any{

    internal val mapBacking: MutableMap<K, V> = mutableMapOf()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = mapBacking.entries

    private val overwriteMsg: String = "Provided key already exists. Value overwritten"

    override fun put(key: K, value: V): V? {
        return  mapBacking.getOrPut(key){  value }
    }

    fun register(key: K, value: V):V?{
        return mapBacking.getOrPut(key){
            overwriteMsg.output(Colour.YellowBright)
            value
        }
    }
}
