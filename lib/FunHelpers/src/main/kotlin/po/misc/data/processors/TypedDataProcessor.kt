package po.misc.data.processors

import po.misc.data.PrintableBase

class TypedDataProcessor<T: PrintableBase<T>>(
    override val topEmitter: TypedDataProcessorBase<*>? = null
):TypedDataProcessorBase<T>() {


}