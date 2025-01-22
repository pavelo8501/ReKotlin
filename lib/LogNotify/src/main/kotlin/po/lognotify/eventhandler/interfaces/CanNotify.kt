package po.lognotify.eventhandler.interfaces

import po.lognotify.eventhandler.EventHandlerBase

interface CanNotify{

    // fun init(moduleName: String) = eventHandler.init(moduleName)

    infix  fun  infoMessage(message: String) = eventHandler.info(message)

    fun info(message: String) = eventHandler.info(message)
    fun <T: Any>action(message: String, fn:()-> T?) = eventHandler.action(message,fn)
    fun notifyError(message: String) =  eventHandler.notifyError(message)

    val eventHandler: EventHandlerBase

}