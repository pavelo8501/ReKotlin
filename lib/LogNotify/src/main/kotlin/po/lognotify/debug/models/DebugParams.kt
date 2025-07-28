package po.lognotify.debug.models

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase


data class DebugParams<D: PrintableBase<D>>(
    val message: String,
    val template:PrintableTemplateBase<D>?
)
