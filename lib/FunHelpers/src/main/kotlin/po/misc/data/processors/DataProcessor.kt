package po.misc.data.processors

import po.misc.data.PrintableBase


class DataProcessor(
    override val topEmitter: DataProcessorBase? = null
): DataProcessorBase() {

    fun toEmitter(record: PrintableBase<*>){
        topEmitter?.emitData(record)
    }
}