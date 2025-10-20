package po.misc.data.printable

import po.misc.data.printable.grouping.ArbitraryDataMap
import kotlin.reflect.KClass

interface Printable {
    val formattedString : String
    val ownClass: KClass<out  Printable>
    val arbitraryMap: ArbitraryDataMap<Printable>
    fun echo()
}

