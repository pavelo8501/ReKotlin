package po.misc.data.console

import po.misc.interfaces.ValueBased

class PrintableTemplate<T: PrintableBase<T>>(override val value: Int, val template: T.()-> String) : ValueBased