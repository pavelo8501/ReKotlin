package po.misc.data.printable.grouping

import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull
import po.misc.data.printable.Printable
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
    override fun toString(): String = "${ownClass.simpleName}${postfix.orDefault()}"
}

class ArbitraryDataMap<V: Printable>(): AbstractMutableMap<ArbitraryKey, MutableList<PrintableBase<*>>>(){

    @PublishedApi
    internal val mapBacking: MutableMap<ArbitraryKey, MutableList<PrintableBase<*>>> = mutableMapOf()

    override val entries: MutableSet<MutableMap.MutableEntry<ArbitraryKey, MutableList<PrintableBase<*>>>>
        get() = mapBacking.entries

    val totalSize: Int get() = mapBacking.values.sumOf {it.size}

    private fun keyFromPrintable(data: PrintableBase<*>):ArbitraryKey{
       return ArbitraryKey(data.ownClass)
    }

    override fun put(key: ArbitraryKey, value: MutableList<PrintableBase<*>>): MutableList<PrintableBase<*>>? {
      return  mapBacking.getOrPut(key){  value }
    }

    fun putPrintable(data: PrintableBase<*>):ArbitraryKey{
        val key = keyFromPrintable(data)

        mapBacking[key]?.add(data) ?:run {
           put(key, mutableListOf(data))
        }
        return key
    }

    fun putPrintable(data:  PrintableBase<*>, postfixBuilder:(PrintableBase<*>)-> String):ArbitraryKey{
        val postfix = postfixBuilder(data)
        val key = ArbitraryKey(data.ownClass).apply { this.postfix = postfix }
        mapBacking[key]?.add(data) ?: run {
            put(key, mutableListOf(data))
        }
        return key
    }
}
