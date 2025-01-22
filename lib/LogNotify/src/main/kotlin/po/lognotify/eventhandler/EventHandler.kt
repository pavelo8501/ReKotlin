package po.lognotify.eventhandler

import po.lognotify.eventhandler.exceptions.NotificatorServiceException
import po.lognotify.eventhandler.interfaces.HandlerStatics
import po.lognotify.eventhandler.models.Event
import po.lognotify.shared.enums.HandleType
import po.lognotify.shared.enums.SeverityLevel
import po.lognotify.shared.exceptions.HandledThrowable
import java.util.concurrent.CopyOnWriteArrayList


class RootEventHandler(moduleName: String): EventHandlerBase(moduleName){

    init {
        handleEvent(helper.newInfo("$moduleName Notify Service Started"))
    }

    fun getEvent(wipeData: Boolean = true): Event?{
        val currentEventCopy = currentEvent?.let { event ->
            Event(
                module = event.module,
                msg = event.msg,
                type = event.type
            ).also {
                it.setElapsed(event.startTime, event.stopTime)
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
    override val moduleName: String,
    val parent : EventHandlerBase? = null
) :  HandlerStatics
{

    var routedName: String = moduleName
    val eventQue = CopyOnWriteArrayList<Event>()
    var currentEvent : Event? = null
        private set

    val helper = HandlerStatics

    init {
        if(parent != null){
            routedName = "${parent.routedName}|$moduleName"
        }
        helper.init(routedName)
    }

    private fun registerEvent(event: Event){
        eventQue.add(event)
        currentEvent = event
    }

    private fun  handleException(ex: HandledThrowable){
        handleEvent(Event(routedName, helper.handledMsg(ex), SeverityLevel.EXCEPTION))
        throw ex
    }

    @Synchronized
    protected fun handleEvent(event: Event){
        if (parent == null) {
            registerEvent(event)
        } else {
            parent.currentEvent?.let { hostingEvent ->
                hostingEvent.subEvents.add(event)
            } ?: parent.handleEvent(event)
        }
    }


    protected fun <T: Any?>processAndMeasure(event : Event, fn:()-> T?):T?{
        try {
            val res =  fn.invoke()
            event.stopTimer()
            handleEvent(event)
            return res
        }catch (ex: HandledThrowable){
            when(ex.type){
                HandleType.SKIP_SELF -> {

                }
                HandleType.CANCEL_ALL -> {

                }
                HandleType.PROPAGATE_TO_PARENT -> {
                    handleException(ex)
                    throw ex
                }
            }
            return null
        // Catching generic Exception as a safeguard against unexpected errors
        }catch(ex: Exception) {
            helper.unhandledMsg(ex)
            throw ex
        }
    }

    fun info(message: String){
        handleEvent(Event(routedName, message, SeverityLevel.INFO))
    }

    fun <T: Any?>action(message: String, fn:()-> T?):T?{
        return processAndMeasure(helper.newInfo(message), fn)
    }
    fun action(message: String, fn:()-> Unit){
        processAndMeasure(helper.newInfo(message)){
            fn()
            null
        }
    }


    fun notifyError(message: String){
        handleEvent(Event(routedName, message, SeverityLevel.EXCEPTION))
    }

    fun wipeData(){
        eventQue.clear()
        currentEvent?.subEvents?.clear()
        currentEvent = null
    }

}


