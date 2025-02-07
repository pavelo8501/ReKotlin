package po.exposify.components.eventhandler

import po.exposify.components.eventhandler.enums.EventType
import po.exposify.components.eventhandler.models.Event
import java.util.concurrent.CopyOnWriteArrayList

class RootEventHandler(moduleName: String): EventHandlerBase(moduleName){
    fun getEvent(wipeData: Boolean = true): Event?{
        val currentEventCopy = currentEvent?.let { event ->
            Event(
                module = event.module,
                msg = event.msg,
                type = event.type,
                timestamp = event.timestamp
            ).also {
                it.setElapsed(event.elapsedMills ?: 0)
                it.subEvents.addAll(event.subEvents)
            }
        }
        if (wipeData) {
            this.wipeData()
        }
        return currentEventCopy
    }
}

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

    @Synchronized
    fun handleEvent(event: Event){
        if (parentHandler == null) {
            registerEvent(event)
        } else {
            parentHandler.currentEvent?.let { hostingEvent ->
                hostingEvent.subEvents.add(event)
            } ?: parentHandler.handleEvent(event)
        }
    }

    /**
     * Notify on the event with no performance statistics
     */
    fun notify(message: String){
        handleEvent(Event(routedName, message, EventType.INFO,  System.currentTimeMillis()))
    }

    fun <T: Any>notify(message: String, fn:()-> T?):T?{
        val startMills = System.nanoTime()
        val event = Event(routedName, message, EventType.INFO, startMills)
        handleEvent(event)
        val res =  fn.invoke()
        val elapsedMills = (System.nanoTime() - startMills)
        event.setElapsed(elapsedMills)
        return res
    }

    fun notifyError(message: String){
        handleEvent(Event(routedName, message, EventType.ERROR,  System.currentTimeMillis()))
    }

    fun wipeData(){
        eventQue.clear()
        currentEvent?.subEvents?.clear()
        currentEvent = null
    }
}