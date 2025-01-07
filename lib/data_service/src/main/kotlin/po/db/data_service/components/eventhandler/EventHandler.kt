package po.db.data_service.components.eventhandler


import po.db.data_service.components.eventhandler.enums.EventType
import po.db.data_service.components.eventhandler.models.Event
import java.util.concurrent.CopyOnWriteArrayList


class RootEventHandler(moduleName: String): EventHandlerBase(moduleName)

class EventHandler(
    moduleName: String,
    parentHandler: EventHandlerBase
): EventHandlerBase(moduleName, parentHandler){

//    override fun notify(message: String) {
//        val thisEvent = Event(moduleName, message, EventType.INFO, System.currentTimeMillis())
//        if(parentHandler == null){
//            super.notify(message)
//        }else{
//            parentHandler.apply{
//                currentEvent?.let { hostingEvent->
//                    hostingEvent.subEvents.add(thisEvent)
//                }?: notify()
//            }
//        }
//    }
}

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

    fun handleEvent(event: Event){
        if(parentHandler == null){
            registerEvent(event)
        }else{
            parentHandler.apply{
                currentEvent?.let { hostingEvent->
                    hostingEvent.subEvents.add(event)
                }?: handleEvent(event)
            }
        }
    }

    open fun notify(message: String){
        handleEvent(Event(routedName, message, EventType.INFO,  System.currentTimeMillis()))
    }
    fun notifyError(message: String){
        handleEvent(Event(routedName, message, EventType.ERROR,  System.currentTimeMillis()))
    }
}