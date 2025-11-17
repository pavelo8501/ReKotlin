package po.misc.data.processors

import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase



interface DataProcessingHooks<T: Printable> {
    fun onDataReceived(dataReceivedCallback: (T)-> Unit)
    fun onSubDataReceived(subDataReceivedCallback: (T, DataProcessorBase<T>)-> Unit)
    fun onArbitraryDataReceived(arbitraryDataReceivedCallback: (PrintableBase<*>)-> Unit)
}