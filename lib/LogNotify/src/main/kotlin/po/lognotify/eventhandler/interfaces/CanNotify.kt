package po.lognotify.eventhandler.interfaces

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import po.lognotify.eventhandler.EventHandlerBase

interface CanNotify{

    val eventHandler: EventHandlerBase

    infix  fun infoMessage(message: String) = eventHandler.info(message)
    fun info(message: String) = eventHandler.info(message)
    suspend fun <T: Any>action(message: String,  fn: suspend ()-> T?) = coroutineScope{
        eventHandler.action<T>(message, fn) }

    fun notifyError(message: String) =  eventHandler.notifyError(message)

}