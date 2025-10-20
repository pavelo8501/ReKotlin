package po.misc.data.processors

import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase

class ProcessorHooks<T: Printable> : DataProcessingHooks<T> {

    internal var dataReceived: ((T) -> Unit)? = null
    override fun onDataReceived(dataReceivedCallback: (T)-> Unit){
        dataReceived = dataReceivedCallback
    }

    internal var subDataReceived: ((T, DataProcessorBase<T>) -> Unit)? = null
    override fun onSubDataReceived(subDataReceivedCallback: (T, DataProcessorBase<T>)-> Unit){
        subDataReceived = subDataReceivedCallback
    }

    internal var arbitraryDataReceived: ((PrintableBase<*>)-> Unit)? = null
    override fun onArbitraryDataReceived(arbitraryDataReceivedCallback: (PrintableBase<*>)-> Unit){
        arbitraryDataReceived = arbitraryDataReceivedCallback
    }
}