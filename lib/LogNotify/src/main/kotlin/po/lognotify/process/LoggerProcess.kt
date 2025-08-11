package po.lognotify.process

import po.misc.containers.ReceiverContainer
import po.misc.context.CTX
import po.misc.coroutines.CoroutineHolder
import po.misc.data.logging.LogCollector
import po.misc.data.printable.PrintableBase
import kotlin.coroutines.CoroutineContext

interface LoggerProcess<T>: ReceiverContainer<T>, CoroutineHolder where T: CTX,  T: LogCollector{
    override val receiver: T
    override val coroutineContext: CoroutineContext

    fun onDataReceived(callback: (PrintableBase<*>)-> Unit)

    fun <T: CoroutineContext.Element> getCoroutineElement(key: CoroutineContext.Key<T>): T?

}