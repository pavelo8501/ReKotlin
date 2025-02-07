package po.exposify.components.eventhandler.interfaces

import po.exposify.components.eventhandler.EventHandlerBase

interface CanNotify{

   // fun init(moduleName: String) = eventHandler.init(moduleName)
    fun notify(message: String) = eventHandler.notify(message)
    fun <T: Any>notify(message: String, fn:()-> T?) = eventHandler.notify(message,fn)
    fun notifyError(message: String) =  eventHandler.notifyError(message)

    val eventHandler: EventHandlerBase

}