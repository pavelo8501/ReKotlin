package po.db.data_service.components.eventhandler.interfaces

import po.db.data_service.components.eventhandler.EventHandler

interface CanNotify{

   // fun init(moduleName: String) = eventHandler.init(moduleName)
    fun notify(message: String) = eventHandler.notify(message)
    fun notifyError(message: String) =  eventHandler.notifyError(message)


    val eventHandler: EventHandler


}