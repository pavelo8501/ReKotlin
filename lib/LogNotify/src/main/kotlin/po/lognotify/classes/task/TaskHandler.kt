package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.interfaces.HandledTask
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.misc.data.processors.DataProcessor
import po.misc.data.processors.DataProcessorBase
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.types.UpdateType


class TaskHandler<R: Any?>(
    val task : TaskBase<*, R>,
    val exceptionHandler: ExceptionHandler<*, R>,
    internal val dataProcessor: LoggerDataProcessor
): HandledTask<R>{

    override val notifier: NotifierBase get() = task.notifier


    fun infoV2(message: String): TaskData{
       return dataProcessor.info(message)
    }
    fun warnV2(message: String): TaskData{
        return dataProcessor.warn(message)
    }
    fun warnV2(ex: ManagedException, message: String): TaskData{
        return dataProcessor.warn(ex, message)
    }

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


    fun subscribeTaskEvents(handler: UpdateType, callback: (LoggerStats) -> Unit) {
        task.callbackRegistry[handler] = callback
    }

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

