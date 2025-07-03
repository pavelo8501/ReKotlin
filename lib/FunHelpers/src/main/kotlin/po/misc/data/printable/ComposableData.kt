package po.misc.data.printable

import po.misc.data.printable.PrintableBase
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.ValueBased

interface ComposableData {
   // val itemId: ValueBased
    val emitter:  IdentifiableContext
    val children: List<ComposableData>
    val parentRecord: Printable? get() = null
    val type: ValueBased? get() = null

    fun setParent(parent: PrintableBase<*>)
}