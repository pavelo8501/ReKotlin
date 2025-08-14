package po.lognotify.process

import kotlinx.coroutines.CoroutineScope
import po.lognotify.notification.models.LogData
import po.misc.context.CTX
import po.misc.coroutines.CoroutineHolder
import po.misc.interfaces.Processable
import kotlin.coroutines.CoroutineContext


interface LoggerProcess<T>: CoroutineHolder, CoroutineContext.Element, CTX where T : Processable{

    val handler:ProcessHandler
    override val scope: CoroutineScope

    fun CTX.onDataReceived(callback: (LogData)-> Unit)
    fun <T: CoroutineContext.Element> getCoroutineElement(key: CoroutineContext.Key<T>): T?

}