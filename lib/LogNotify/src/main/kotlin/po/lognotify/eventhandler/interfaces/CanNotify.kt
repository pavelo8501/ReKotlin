package po.lognotify.eventhandler.interfaces

import kotlinx.coroutines.coroutineScope
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.exceptions.ProcessableException

interface CanNotify{

    val eventHandler: EventHandlerBase

    infix  fun infoMessage(message: String) = eventHandler.info(message)
    fun info(message: String) = eventHandler.info(message)

    suspend fun <T: Any>action(message: String,  fn: suspend ()-> T?) = coroutineScope{
        eventHandler.action<T>(message, fn) }

    fun warn(message: String) = eventHandler.warn(message)
    fun notifyError(message: String) =  eventHandler.error(message)


    fun <E: ProcessableException> propagatedException(message: String?, block: (E.()->Unit)? = null)
        = eventHandler.raisePropagateException(message,block)

}