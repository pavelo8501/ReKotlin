package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.task.interfaces.HandledTask
import po.lognotify.exceptions.ExceptionHandler
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException


class TaskHandler<R: Any?>(
    val task : TaskBase<R>,
    val exceptionHandler: ExceptionHandler<R>,
): HandledTask<R>{

    override val notifier: NotifierBase get() = task.notifier

    fun echo(message: String){
        notifier.echo(message)
    }
    fun info(message: String){
        notifier.info(message)
    }
    fun warn(message: String){
        notifier.warn(message)
    }
    fun warn(th : Throwable, message: String) = notifier.warn(th, message)


    suspend fun handleFailure( vararg  handlers: HandlerType, fallbackFn: suspend (exception: ManagedException)->R){
        exceptionHandler.provideAsyncHandlerFn(handlers.toSet() , fallbackFn)
    }

    inline fun <T, R2>  withTaskContext(receiver: T,  crossinline block : suspend T.() -> R2):R2{
        var result: R2? = null
        runBlocking {
            val job = launch(start = CoroutineStart.UNDISPATCHED, context = task.coroutineContext) {
                result = block(receiver)
            }
            job.join()
        }
        return result as R2
    }
}

