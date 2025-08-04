package po.lognotify.interfaces

import po.misc.containers.ReceiverContainer
import po.misc.context.CTX
import po.misc.coroutines.CoroutineHolder
import po.misc.data.printable.PrintableBase
import kotlin.coroutines.CoroutineContext



interface LoggerProcess<T: CTX>: ReceiverContainer<T>, CoroutineHolder {
    override val receiver: T
    override val coroutineContext: CoroutineContext

    fun onDataReceived(callback: (PrintableBase<*>)-> Unit)


}