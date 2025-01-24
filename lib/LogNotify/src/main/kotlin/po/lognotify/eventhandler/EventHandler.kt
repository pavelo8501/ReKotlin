package po.lognotify.eventhandler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import po.lognotify.eventhandler.components.ExceptionHandler
import po.lognotify.eventhandler.components.ExceptionHandlerInterface
import po.lognotify.eventhandler.exceptions.NotificatorUnhandledException
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.interfaces.HandlerStatics
import po.lognotify.eventhandler.models.Event
import po.lognotify.shared.enums.HandleType
import po.lognotify.shared.enums.SeverityLevel
import java.util.concurrent.CopyOnWriteArrayList


class RootEventHandler(
    moduleName: String
): EventHandlerBase(moduleName)
{
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
    val parent : EventHandlerBase? = null,
    val exceptionHandler : ExceptionHandler = ExceptionHandler()
) :  HandlerStatics,  ExceptionHandlerInterface by exceptionHandler
{

    var routedName: String = moduleName
    val eventQue = CopyOnWriteArrayList<Event>()
    var currentEvent : Event? = null
        private set

    val helper = HandlerStatics

    protected val notifierScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    @Synchronized
    internal  fun handleException(ex: ProcessableException){
        handleEvent(Event(routedName).setException(ex))
    }

    @Synchronized
    internal fun handleEvent(event: Event){
        if (parent == null) {
            registerEvent(event)
        } else {
            parent.currentEvent?.subEvents?.add(event) ?: parent.handleEvent(event)
        }
    }

    internal suspend inline fun <T: Any?>processAndMeasure(event : Event, fn: suspend ()-> T?):T?{
        try {
            val res = fn.invoke()
            event.stopTimer()
            notifierScope.launch {
                handleEvent(event)
            }
            return res
        } catch (ex: ProcessableException) {
            when (ex.handleType) {
                HandleType.SKIP_SELF -> {
                    handleException(ex)
                }
                HandleType.CANCEL_ALL -> {
                    ex.cancellationFn?.let {
                        it.invoke()
                        handleException(ex)
                    }?: warn(helper.msg("Cancel function not set", ex))
                }
                HandleType.PROPAGATE_TO_PARENT -> {
                    handleException(ex)
                    throw ex
                }
            }
            return null
            // Catching generic Exception as a safeguard against unexpected errors
        } catch (ex: Exception) {
            error(helper.unhandledMsg(ex))
            throw NotificatorUnhandledException(helper.unhandledMsg(ex), ex)
        }
    }

    fun info(message: String){
        handleEvent(Event(routedName, message, SeverityLevel.INFO))
    }
    fun warn(message: String){
        notifierScope.launch {
            handleEvent(Event(routedName, message, SeverityLevel.WARNING))
        }
    }

    suspend fun <T: Any?> action(message: String,  fn: suspend ()-> T?):T?{
        return processAndMeasure(helper.newEvent(message), fn)
    }
    @JvmName("eventActionNoReturn")

    suspend fun action(message: String, fn: suspend ()-> Unit){
        processAndMeasure(helper.newEvent(message)){
            fn.invoke()
        }
        null
    }

    fun error(message: String){
        notifierScope.launch {
            handleEvent(Event(routedName, message, SeverityLevel.EXCEPTION))
        }
    }

    fun wipeData(){
        eventQue.clear()
        currentEvent?.subEvents?.clear()
        currentEvent = null
    }
}


