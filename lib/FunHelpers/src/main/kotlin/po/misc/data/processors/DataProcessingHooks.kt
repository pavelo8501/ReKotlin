package po.misc.data.processors

import po.misc.data.printable.PrintableBase

interface DataProcessingHooks<T: PrintableBase<T>> {
    fun onDataReceived(dataReceivedCallback: (T)-> Unit)
    fun onSubDataReceived(subDataReceivedCallback: (T, DataProcessorBase<T>)-> Unit)
    fun onArbitraryDataReceived(arbitraryDataReceivedCallback: (PrintableBase<*>)-> Unit)
}