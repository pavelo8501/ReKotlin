package po.db.data_service.components.eventhandler

import po.db.data_service.components.eventhandler.enums.EventType
import po.db.data_service.components.eventhandler.models.Event
import java.util.concurrent.CopyOnWriteArrayList

class RootEventHandler(moduleName: String): EventHandlerBase(moduleName)

class EventHandler(
    moduleName: String,
    parentHandler: EventHandlerBase
): EventHandlerBase(moduleName, parentHandler)

sealed class EventHandlerBase(
    val moduleName: String,
    val parentHandler : EventHandlerBase? = null
) {

    var routedName: String = moduleName
    val eventQue = CopyOnWriteArrayList<Event>()
    var currentEvent : Event? = null
        private set

    init {
        if(parentHandler != null){
          routedName = "${parentHandler.routedName}|$moduleName"
        }
    }

    private fun registerEvent(event: Event){
        eventQue.add(event)
        currentEvent = event
    }

    fun handleEvent(event: Event): Event{
//        if(parentHandler == null){
//           registerEvent(event)
//           return event
//        }
//        parentHandler.apply{
//            currentEvent?.let { hostingEvent->
//                hostingEvent.subEvents.add(event)
//            }?: handleEvent(event)
//        }
//        return event

        val startMills = System.currentTimeMillis()
        val processedEvent = if (parentHandler == null) {
            registerEvent(event)
            event
        } else {
            parentHandler.currentEvent?.let { hostingEvent ->
                hostingEvent.subEvents.add(event)
                event
            } ?: parentHandler.handleEvent(event)
        }
        processedEvent.setElapsed(System.currentTimeMillis() - startMills)
        return processedEvent
    }

    /**
     * Notify on the event with no performance statistics
     */
    fun notify(message: String){
        handleEvent(Event(routedName, message, EventType.INFO,  System.currentTimeMillis()))
    }

    fun <T: Any>notify(message: String, fn:()-> T?):T?{
        val startMills = System.currentTimeMillis()
        val event = handleEvent(Event(routedName, message, EventType.INFO,  System.currentTimeMillis()))
        val res =  fn.invoke()
        val elapsedMills = ( System.currentTimeMillis() - startMills)
        event.setElapsed(elapsedMills)
        return res
    }

    fun notifyError(message: String){
        handleEvent(Event(routedName, message, EventType.ERROR,  System.currentTimeMillis()))
    }
}