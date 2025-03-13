package po.lognotify.eventhandler.interfaces

import kotlinx.coroutines.coroutineScope
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.exceptions.SkipException
import po.lognotify.shared.enums.HandleType

interface CanNotify{

    val eventHandler: EventHandlerBase

    fun info(message: String) = eventHandler.info(message)
    suspend fun <T: Any?>task(message: String,  fn: suspend ()-> T?) :T?     =
        eventHandler.task<T>(message, fn)

    fun warn(message: String) = eventHandler.warn(message)

    fun throwPropagate(message: String) = eventHandler.throwPropagateException(message)
    fun throwSkip(message: String)  = eventHandler.throwSkipException(message)
    fun throwCancel(message: String,  cancelFn: (() -> Unit)? = null)
        = eventHandler.throwCancelException(message, cancelFn)


}