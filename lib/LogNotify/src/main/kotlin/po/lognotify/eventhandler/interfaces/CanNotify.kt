package po.lognotify.eventhandler.interfaces

import kotlinx.coroutines.coroutineScope
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.exceptions.SkipException
import po.lognotify.shared.enums.HandleType

interface CanNotify{

    val eventHandler: EventHandlerBase

    infix  fun infoMessage(message: String) = eventHandler.info(message)
    fun info(message: String) = eventHandler.info(message)

    suspend fun <T: Any>action(message: String,  fn: suspend ()-> T?) = coroutineScope{
        eventHandler.task<T>(message, fn) }

    fun warn(message: String) = eventHandler.warn(message)
    fun notifyError(message: String) =  eventHandler.warn(message)


    fun <E: ProcessableException> throwPropagated(message: String?, block: (E.()->Unit)? = null)
        = eventHandler.raisePropagateException(message,block)

    fun throwSkip(message: String?): SkipException{
        return SkipException(message.toString())
    }

}