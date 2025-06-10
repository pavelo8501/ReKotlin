package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.action.ActionSpan
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.interfaces.HandledTask
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.misc.exceptions.ManagedException
import po.misc.types.UpdateType


class TaskHandler<R: Any?>(
    val task : TaskBase<*, R>,
    internal val dataProcessor: LoggerDataProcessor
): HandledTask<R>{

    val actions : List<ActionSpan<*,*>> get()= task.actionSpans

    fun echo(message: String){
        dataProcessor.info(message)
    }

    fun info(message: String): TaskData{
       return dataProcessor.info(message)
    }
    fun warn(message: String): TaskData{
        return dataProcessor.warn(message)
    }
    fun warn(ex: ManagedException, message: String): TaskData{
        return dataProcessor.warn(ex, message)
    }

    fun subscribeTaskEvents(handler: UpdateType, callback: (LoggerStats) -> Unit) {
        task.callbackRegistry[handler] = callback
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

