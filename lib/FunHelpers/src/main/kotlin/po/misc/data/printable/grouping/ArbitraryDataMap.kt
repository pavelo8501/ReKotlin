package po.misc.data.printable.grouping

import po.misc.data.helpers.emptyOnNull
import po.misc.data.printable.PrintableBase
import kotlin.reflect.KClass


class ArbitraryKey(val ownClass: KClass<*>) {
    var postfix: String? = null
    override fun hashCode(): Int {
        return ownClass.hashCode()
    }
    override fun equals(other: Any?): Boolean {
        return other is ArbitraryKey &&
                ownClass == other.ownClass
    }
    override fun toString(): String = "${ownClass.simpleName}${postfix.emptyOnNull()}"
}

class ArbitraryDataMap<V: PrintableBase<*>>(): AbstractMutableMap<ArbitraryKey, MutableList<V>>(){

    @PublishedApi
    internal val mapBacking: MutableMap<ArbitraryKey, MutableList<V>> = mutableMapOf()

    override val entries: MutableSet<MutableMap.MutableEntry<ArbitraryKey, MutableList<V>>>
        get() = mapBacking.entries

    val totalSize: Int get() = mapBacking.values.sumOf {it.size}

    private fun keyFromPrintable(data: V):ArbitraryKey{
       return ArbitraryKey(data.ownClass)
    }

    override fun put(key: ArbitraryKey, value: MutableList<V>): MutableList<V>? {
      return  mapBacking.getOrPut(key){  value }
    }

    fun putPrintable(data: V):ArbitraryKey{
        val key = keyFromPrintable(data)
        mapBacking[key]?.add(data) ?:run {
           put(key, mutableListOf(data))
        }
        return key
    }

    fun putPrintable(data: V, postfixBuilder:(V)-> String):ArbitraryKey{
        val postfix = postfixBuilder(data)
        val key = ArbitraryKey(data.ownClass).apply { this.postfix = postfix }
        mapBacking[key]?.add(data) ?: run {
            put(key, mutableListOf(data))
        }
        return key
    }
}
