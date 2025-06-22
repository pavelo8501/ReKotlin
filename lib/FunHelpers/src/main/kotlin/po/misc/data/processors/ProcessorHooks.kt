package po.misc.data.processors

import po.misc.data.PrintableBase

class ProcessorHooks<T: PrintableBase<T>>(
   internal var onData: ((T) -> Unit)? = null,
   internal var onArbitraryData: ((PrintableBase<*>)-> Unit)? = null
) {

    fun dataReceived(hook: (T)-> Unit){
        onData = hook
    }

    fun arbitraryDataReceived(hook: (PrintableBase<*>)-> Unit){
        onArbitraryData = hook
    }

}