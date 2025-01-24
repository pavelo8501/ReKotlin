package po.lognotify.eventhandler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import po.lognotify.eventhandler.exceptions.CancelException
import po.lognotify.eventhandler.exceptions.NotificatorUnhandledException
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.exceptions.PropagateException
import po.lognotify.eventhandler.exceptions.SkipException
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
    val parent : EventHandlerBase? = null
) :  HandlerStatics
{

    private var skipExceptionConstructorFn : (()->ProcessableException) =
        { SkipException("Default skip message").apply { handleType = HandleType.SKIP_SELF } }

    private var cancelExceptionConstructorFn : (()->ProcessableException) =
        { CancelException("Default skip message").apply { handleType = HandleType.SKIP_SELF } }

    private var propagateExceptionConstructorFn : (()->ProcessableException) =
        { PropagateException("Default skip message").apply { handleType = HandleType.PROPAGATE_TO_PARENT} }

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

    protected fun handleException(ex: ProcessableException, handleType: HandleType? = null ){
        if(handleType!= null){
            ex.handleType =  handleType
        }
        handleEvent(Event(routedName).setException(ex))
        throw ex
    }

    fun <E: ProcessableException> registerSkipException(exConstructFn : ()->E){
        skipExceptionConstructorFn = exConstructFn
    }

    fun <E: ProcessableException> registerCancelException(exConstructFn : ()->E){
        cancelExceptionConstructorFn = exConstructFn
    }

    fun <E: ProcessableException> registerPropagateException(exConstructFn : ()->E){
        propagateExceptionConstructorFn = exConstructFn
    }

    @Synchronized
    protected fun handleEvent(event: Event){
        if (parent == null) {
            registerEvent(event)
        } else {
            parent.currentEvent?.subEvents?.add(event) ?: parent.handleEvent(event)
        }
    }
    protected suspend inline fun <T: Any?>processAndMeasure(event : Event, fn: suspend ()-> T?):T?{
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
                    TODO("Logic for skip exceptions")
                }
                HandleType.CANCEL_ALL -> {
                    TODO("Logic for cancel exceptions")
                }
                HandleType.PROPAGATE_TO_PARENT -> {
                    TODO("Logic for propagate exceptions")
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

    fun raiseSkipException(msg: String? = null){
        val skipException = skipExceptionConstructorFn.invoke()
        skipException.handleType = HandleType.SKIP_SELF
        skipException.message = msg?:skipException.message
        throw skipException
    }

    fun raiseCancelException(msg: String? = null){
        val cancelException = cancelExceptionConstructorFn.invoke()
        cancelException.handleType = HandleType.CANCEL_ALL
        cancelException.message = msg?:cancelException.message
        throw cancelException
    }

    fun raisePropagateException(msg: String? = null){
        val propagateException = propagateExceptionConstructorFn.invoke()
        propagateException.handleType = HandleType.PROPAGATE_TO_PARENT
        propagateException.message = msg?:propagateException.message
        throw propagateException
    }

    fun wipeData(){
        eventQue.clear()
        currentEvent?.subEvents?.clear()
        currentEvent = null
    }
}


