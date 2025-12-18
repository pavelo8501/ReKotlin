package po.misc.data.printable

import po.misc.data.KeyedValue
import po.misc.data.printable.grouping.ArbitraryDataMap

interface ComposableData<T: Printable> {

    val arbitraryMap: ArbitraryDataMap<T>
    val type: KeyedValue? get() = null
    fun setParent(parent: Printable):Printable
}