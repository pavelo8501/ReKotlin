package po.lognotify.eventhandler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    override val isParent : Boolean = true

    var onPropagateExceptionFn: (()->Unit)? = null

    init {
        handleEvent(helper.newInfo("$moduleName Notify Service Started"))
    }

    fun onPropagateException(callback:()->Unit){
        onPropagateExceptionFn = callback
    }

    fun handlePropagatedException(ex: ProcessableException){
        if (onPropagateExceptionFn != null) {
            onPropagateExceptionFn!!.invoke()
        } else {
            warn("Propagate to parent Exception handed but no onPropagateException action is set."
                    +"Rethrowing exception")
            throw ex
        }
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
): EventHandlerBase(moduleName, parentHandler){
     override val isParent : Boolean = false
}

sealed class EventHandlerBase(
    override val moduleName: String,
    val parent : EventHandlerBase? = null,
    val exceptionHandler : ExceptionHandler = ExceptionHandler()
) :  HandlerStatics,  ExceptionHandlerInterface by exceptionHandler
{

    protected abstract val isParent : Boolean
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

    internal suspend inline fun <T: Any?>processAndMeasure(
        asyncMode: Boolean,  event : Event, controlledProcessFn: suspend ()-> T?
    ):T? {
        try {
            val res = controlledProcessFn.invoke()
            event.stopTimer()
            if(asyncMode){
                notifierScope.launch { handleEvent(event) }
            }else{
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
                    if (isParent) {
                        (this as RootEventHandler).handlePropagatedException(ex)
                    }else{
                        throw ex
                    }
                }
            }
            return null
            // Catching generic Exception as a safeguard against unexpected errors
        } catch (ex: Exception) {
            error(helper.unhandledMsg(ex))
            throw NotificatorUnhandledException(helper.unhandledMsg(ex), ex)
        }
    }

    fun <T: Any?> actionAsync(message: String, fn: suspend () -> T?): Deferred<T?> {
        return CoroutineScope(Dispatchers.IO).async {
            processAndMeasure(true, helper.newEvent(message), fn)
        }
    }

    fun <T: Any?> action(message: String, fn: suspend () -> T?): T? {
        return runBlocking {
            async { processAndMeasure(false, helper.newEvent(message), fn) }.await()
        }
    }

    @JvmName("eventActionAsyncNoReturn")
    fun actionAsync(message: String, fn: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            processAndMeasure(true, helper.newEvent(message)) { fn.invoke() }
        }
    }

    @JvmName("eventActionNoReturn")
    fun action(message: String, fn: suspend () -> Unit) {
        runBlocking {
            processAndMeasure(false, helper.newEvent(message)) { fn.invoke() }
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


