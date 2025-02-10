package po.lognotify.eventhandler

import po.lognotify.eventhandler.components.ExceptionHandler
import po.lognotify.eventhandler.components.ExceptionHandlerInterface
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.exceptions.UnmanagedException
import po.lognotify.eventhandler.interfaces.HandlerStatics
import po.lognotify.eventhandler.models.Event
import po.lognotify.shared.enums.HandleType
import po.lognotify.shared.enums.SeverityLevel
import java.util.concurrent.CopyOnWriteArrayList


/**
 * The root event handler responsible for managing the top-level event hierarchy.
 * It serves as the entry point for event processing and ensures that all child events
 * are properly recorded and managed.
 *
 * @param moduleName The name of the module associated with this event handler.
 */
class RootEventHandler(
    moduleName: String
): EventHandlerBase(moduleName, null) {

    /** Indicates that this handler is the root of the event hierarchy. */
    override val isParent: Boolean get() = true

    /** A thread-safe queue storing all completed events. */
    val eventQue = CopyOnWriteArrayList<Event>()

    /** A callback function invoked when an exception needs to be propagated to the parent handler. */
    var onPropagateExceptionFn: (()->Unit)? = null

    /**
     * Registers a callback function to be executed when an exception is propagated to the parent.
     * @param callback The function to be executed on propagation.
     */
    fun onPropagateException(callback:()->Unit){
        onPropagateExceptionFn = callback
    }

    /**
    * Handles exceptions that need to be propagated to the parent.
    * If no callback is set, the exception is logged and rethrown.
    * @param ex The exception to be propagated.
    * @throws ProcessableException if no handler is set.
    */
    fun handlePropagatedException(ex: ProcessableException){
        onPropagateExceptionFn?.invoke() ?: run {
            warn("Propagate to parent Exception handled but no onPropagateException action is set. " +
                    "Rethrowing exception")
            throw ex
        }
    }

    /** Clears all stored events. */
    override fun wipeData(){
        eventQue.clear()
    }
}

/**
 * A child event handler responsible for processing sub-events within the event hierarchy.
 *
 * @param moduleName The name of the module associated with this handler.
 * @param parent The parent event handler that this handler reports to.
 */
class EventHandler(
    moduleName: String,
    parent: EventHandlerBase
): EventHandlerBase(moduleName, parent){
     override val isParent : Boolean = false

    /** Clears the active task and its sub-events. */
    override fun wipeData() {
        activeTask?.subEvents?.clear()
        activeTask = null
    }
}

/**
 * The base class for all event handlers, managing event registration, task execution,
 * and exception handling.
 *
 * @param moduleName The name of the module associated with this handler.
 * @param parent The parent event handler in the hierarchy (null if this is the root).
 * @param exceptionHandler The exception handler used for processing errors.
 */
sealed class EventHandlerBase(
    override val moduleName: String,
    val parent: EventHandlerBase?,
    val exceptionHandler : ExceptionHandler = ExceptionHandler()
) :  HandlerStatics,  ExceptionHandlerInterface by exceptionHandler {

    /** Indicates whether this handler is a parent handler. */
    open val isParent: Boolean get() = false

    /** The full hierarchical name of the handler, including parent names. */
    var routedName: String = moduleName

    /** The currently active task being processed. */
    internal var activeTask : Event? = null

    /** A helper object providing static utilities. */
    val helper = HandlerStatics

    init {
        helper.init(routedName)
        if(parent != null){
            routedName = "${parent.routedName}|$moduleName"
        }
    }

    /**
     * Registers an event and assigns it to the active task or propagates it to the parent.
     * @param event The event to be registered.
     * @return The registered event.
     * @throws UnmanagedException if there is an issue during event registration.
     */
    private fun registerEvent(event: Event): Event{

        //If active task present add to sub events
        activeTask?.subEvents?.add(event) ?: run {
            if (event.type == SeverityLevel.TASK) {
                //If no active tasks and event is a task as active
                activeTask = event
            } else {
                //If not a task propagate to parent
                parent?.activeTask?.subEvents?.add(event) ?: run {
                    throw UnmanagedException(
                        "Parent active task is null when trying to add event: $event",
                        null
                    )
                }
            }
        }
        return event
    }

    /**
     * Finalizes the processing of a task, stopping the timer and assigning it to the correct handler.
     * @param event The task event to be finalized.
     * @throws UnmanagedException if an error occurs during finalization.
     */
    @Synchronized
    protected fun finalizeTask(event: Event){
        if(event.type == SeverityLevel.TASK) {
            event.stopTimer()
            if (this is RootEventHandler) {
                //If this is hierarchy root than add event to que and wipe ot current active event
                eventQue.add(event)
            } else {
                //if not a hierarchy root add to parent active event
                parent?.activeTask?.subEvents?.add(event)?:run {
                    throw UnmanagedException("Parent active task is null when trying to add event: $event")
                }
            }
        }else{
            throw UnmanagedException("Trying to finalize not a Task type event: $event")
        }
        activeTask = null
    }

    /**
     * Handles processable exceptions and determines the appropriate response.
     * @param ex The exception to be handled.
     */
    private fun handleProcessableException(ex: ProcessableException){
        when (ex.handleType) {
            HandleType.SKIP_SELF -> {
                registerEvent(Event(routedName).setException(ex))
            }

            HandleType.CANCEL_ALL -> {
                ex.cancellationFn?.let {cancelFn->
                    cancelFn.invoke()
                    registerEvent(Event(routedName).setException(ex))
                }?: run {
                    registerEvent(Event(routedName, helper.msg("Cancel function not set", ex), SeverityLevel.EXCEPTION))
                    throw  UnmanagedException(helper.unhandledMsg(ex), ex)
                }
            }

            HandleType.PROPAGATE_TO_PARENT -> {
                registerEvent(Event(routedName).setException(ex))
                if (this is RootEventHandler) {
                    handlePropagatedException(ex)
                } else {
                    throw ex
                }
            }
            else -> {throw  ex}
        }
    }

    /**
     * Executes a task within the event handler, ensuring proper event tracking and exception handling.
     * @param message A description of the task.
     * @param taskFn The suspending function representing the task.
     * @return The result of the task execution or null if an exception occurred.
     */
    suspend fun <T: Any?>task(message : String, taskFn: suspend ()-> T?):T? {
        val newTask= registerEvent(helper.newTask(message))
        val result = runCatching {
            taskFn.invoke()
            }.onFailure { ex ->
                when (ex) {
                    is ProcessableException -> {
                        val exceptionEvent = Event(routedName, message, SeverityLevel.EXCEPTION).setException(ex)
                        newTask.subEvents.add(exceptionEvent)
                        handleProcessableException(ex)
                    }
                    else -> {
                        registerEvent(Event(routedName, message, SeverityLevel.EXCEPTION))
                        throw  UnmanagedException(helper.unhandledMsg(ex), ex)
                    }
                }
        }.getOrNull()
        finalizeTask(newTask)
        return result
    }

    /**
     * Logs an informational message as an event.
     * @param message The message to be logged.
     */
    fun info(message: String){
        registerEvent(Event(routedName, message, SeverityLevel.INFO))
    }

    /**
     * Logs a warning message as an event.
     * @param message The message to be logged.
     */
    fun warn(message: String){
        registerEvent(Event(routedName, message, SeverityLevel.WARNING))
    }

    /** Clears event-related data for this handler. */
    abstract fun wipeData()
}


