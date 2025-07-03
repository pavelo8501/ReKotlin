package po.lognotify.debug.models

import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase


data class DebugParams<D: PrintableBase<D>>(
    val message: String,
    val template:PrintableTemplate<D>?
)
