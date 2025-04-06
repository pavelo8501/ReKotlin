package po.managedtask.exceptions


import po.managedtask.classes.notification.NotifyTask
import po.managedtask.classes.task.TaskSealedBase
import po.managedtask.exceptions.enums.CancelType
import po.managedtask.exceptions.enums.DefaultType
import po.managedtask.exceptions.enums.PropagateType


data class ExceptionNotification(
    val taskName: String,
    val nestingLevel : Int,
    val handler: Int,
    val message : String,
    val notifyTask:NotifyTask
)

interface ExceptionsThrown {

    //var onExceptionThrown: ((notification: ExceptionNotification) -> Unit)?

    suspend fun throwDefaultException(message : String): Unit?
    suspend fun throwDefaultException(ex : Throwable): Unit?
    suspend fun throwSkipException(message : String): Unit?
    suspend fun throwCancellationException(message : String, cancelHandler: ((ExceptionBase)-> Unit)? = null): Unit?
    suspend fun throwPropagatedException(message : String): Unit?
    suspend fun subscribeThrowerUpdates(callback: suspend (notification: ExceptionNotification) -> Unit)

}

class ExceptionThrower(
    private val task : TaskSealedBase<*>
) : ExceptionsThrown {

    private var onExceptionThrown: (suspend (notification: ExceptionNotification) -> Unit)? = null
    override suspend fun subscribeThrowerUpdates(callback: suspend (notification: ExceptionNotification) -> Unit){
        onExceptionThrown = callback
    }


    private suspend fun notifyOnException(exception: ExceptionBase){
        val notification = ExceptionNotification(
            task.taskName,
            task.key.nestingLevel,
            exception.handler,
            exception.message.toString(),
            NotifyTask.THROWN)
        onExceptionThrown?.invoke(notification)
    }

    private suspend fun notifyOnThrown(th: Throwable){
        val notification = ExceptionNotification(
            task.taskName,
            task.key.nestingLevel,
            DefaultType.GENERIC.value,
            th.message.toString(),
            NotifyTask.THROWN)
        onExceptionThrown?.invoke(notification)
    }

    override suspend fun throwDefaultException(message : String){
        val ex = DefaultException(message, DefaultType.DEFAULT)
        notifyOnThrown(ex)
        throw ex
    }

    override suspend fun throwDefaultException(th : Throwable){
        val ex =  DefaultException(th.message.toString(), DefaultType.GENERIC)
        ex.setSourceException(th)
        notifyOnThrown(ex)
        throw ex
    }

    override suspend fun throwSkipException(message : String){
        val ex = CancellationException(message, CancelType.SKIP_SELF)
        notifyOnThrown(ex)
        throw ex
    }

    override suspend fun throwCancellationException(
        message: String,
        cancelHandler: ((ExceptionBase) -> Unit)?
    ) {
        val ex =  CancellationException(message, CancelType.CANCEL_ALL)
        if(cancelHandler!=null){
            ex.setCancellationHandler(cancelHandler)
        }
        notifyOnThrown(ex)
        throw  ex
    }

    override suspend fun throwPropagatedException(message : String){
        val ex = PropagatedException(message, PropagateType.PROPAGATED)
        notifyOnThrown(ex)
        throw ex
    }

}