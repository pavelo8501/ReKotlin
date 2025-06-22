package po.misc.data.interfaces

import po.misc.data.PrintableBase
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased


interface ComposableData {
    val itemId: ValueBased
    val emitter: Identifiable
    val children: List<ComposableData>
    val parentRecord: Printable? get() = null
    val type: ValueBased? get() = null

    fun setParent(parent:  PrintableBase<*>)
}

