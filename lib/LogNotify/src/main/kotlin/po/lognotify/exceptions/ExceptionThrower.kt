package po.lognotify.exceptions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.sealed.ProviderThrower
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.enums.HandlerType


interface ExceptionsThrown {

    suspend fun throwDefaultException(message : String): Unit?
    suspend fun throwDefaultException(ex : Throwable): Unit?
    suspend fun throwSkipException(message : String): Unit?
    suspend fun throwCancellationException(message : String, cancelHandler: ((ManagedException)-> Unit)? = null): Unit?
    suspend fun subscribeThrowerUpdates(callback: suspend (notification: Notification) -> Unit)

}

class ExceptionThrower(
    private val task : TaskSealedBase<*>
) : ExceptionsThrown {

    private var onExceptionThrown: (suspend (notification: Notification) -> Unit)? = null
    override suspend fun subscribeThrowerUpdates(callback: suspend (notification: Notification) -> Unit){
        onExceptionThrown = callback
    }


    private suspend fun notifyOnException(exception: Notification){
        val notification = Notification(
            task,
            EventType.EXCEPTION_UNHANDLED,
            SeverityLevel.EXCEPTION,
            exception.message,
            ProviderThrower(task.taskName)
        )
        onExceptionThrown?.invoke(notification)
    }

    private suspend fun notifyOnThrown(th: Throwable){
        val notification = Notification(
            task,
            EventType.EXCEPTION_THROWN,
            SeverityLevel.EXCEPTION,
            th.message.toString()  ,
            ProviderThrower(task.taskName),
        )
        onExceptionThrown?.invoke(notification)
    }

    override suspend fun throwDefaultException(message : String){

//        val ex = ManagedException(message, HandlerType.GENERIC)
        val ex = Exception(message)
        notifyOnThrown(ex)
        throw ex
    }

    override suspend fun throwDefaultException(th : Throwable){
//        val ex =  ManagedException(th.message.toString(), HandlerType.GENERIC)
//        ex.setSourceException(th)
//        val ex = Exception(message)
//        notifyOnThrown(ex)
//        throw ex
//         val ex = Exception(message)
//

        throw th
    }

    override suspend fun throwSkipException(message : String){
       // val ex = CancellationException(message, HandlerType.SKIP_SELF)

        val ex = Exception(message)
        notifyOnThrown(ex)

        throw ex
    }

    override suspend fun throwCancellationException(
        message: String,
        cancelHandler: ((ManagedException) -> Unit)?
    ) {
        //val ex =  CancellationException(message, HandlerType.CANCEL_ALL)
        val ex = Exception(message)
//        if(cancelHandler!=null){
//            ex.setCancellationHandler(cancelHandler)
//        }
        notifyOnThrown(ex)
        throw  ex
    }

}