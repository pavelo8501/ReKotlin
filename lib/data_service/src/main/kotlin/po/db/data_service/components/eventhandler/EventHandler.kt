package po.db.data_service.components.eventhandler


import po.db.data_service.components.eventhandler.enums.EventType
import po.db.data_service.components.eventhandler.models.Event
import java.util.concurrent.CopyOnWriteArrayList


open class EventHandler(val moduleName: String) {

    private val eventQue = CopyOnWriteArrayList<Event>()

    private fun handleEvent(event: Event){
        eventQue.add(event)
    }

    fun notify(message: String){
        handleEvent(Event(moduleName, message, EventType.INFO,  System.currentTimeMillis()))
    }
    fun notifyError(message: String){
        handleEvent(Event(moduleName, message, EventType.ERROR,  System.currentTimeMillis()))
    }

}