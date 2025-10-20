package po.misc.data.printable

import po.misc.data.printable.grouping.ArbitraryDataMap
import po.misc.interfaces.ValueBased

interface ComposableData {
    val arbitraryMap: ArbitraryDataMap<Printable>
    val type: ValueBased? get() = null
    fun setParent(parent: Printable):Printable
}