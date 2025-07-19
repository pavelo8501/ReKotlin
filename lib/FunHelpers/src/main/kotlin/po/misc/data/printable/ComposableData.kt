package po.misc.data.printable

import po.misc.context.CTX
import po.misc.interfaces.ValueBased

interface ComposableData {
   // val itemId: ValueBased
    val producer:  CTX
    val children: List<ComposableData>
    val parentRecord: Printable? get() = null
    val type: ValueBased? get() = null

    fun setParent(parent: PrintableBase<*>)
}