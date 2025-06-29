package po.misc.data.processors

import po.misc.collections.StaticTypeKey
import po.misc.data.printable.PrintableBase

class ProcessorHooks<T: PrintableBase<T>>() {

    internal var onDataReceived: ((T) -> Unit)? = null
    fun dataReceived(hook: (T)-> Unit){
        onDataReceived = hook
    }

    internal var onArbitraryData: ((PrintableBase<*>)-> Unit)? = null
    fun arbitraryDataReceived(hook: (PrintableBase<*>)-> Unit){
        onArbitraryData = hook
    }

    internal var onChildAttached : ((childRecord:PrintableBase<*>, parentRecord:PrintableBase<*>) -> Unit)? = null
    fun childAttached(hook: (childRecord:PrintableBase<*>, parentRecord:PrintableBase<*>) -> Unit){
        onChildAttached = hook
    }

    internal var onDebugListUpdated:((MutableMap<Int, StaticTypeKey<*>>)-> Unit)? = null
    fun debugListUpdated(hook: (MutableMap<Int, StaticTypeKey<*>>)-> Unit){
        onDebugListUpdated = hook
    }

}