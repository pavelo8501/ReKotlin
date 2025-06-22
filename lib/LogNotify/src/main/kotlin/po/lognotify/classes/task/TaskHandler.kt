package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.action.ActionSpan
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.interfaces.HandledTask
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.misc.callbacks.manager.wrapRawCallback
import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplateBase
import po.misc.data.interfaces.Printable
import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable

//typealias PrintTemplate<T> = PrintableBase<T>.(StringBuilder) -> Unit

class TaskHandler<R: Any?>(
    val task : TaskBase<*, R>,
    internal val dataProcessor: LoggerDataProcessor,
    val identifiable: Identifiable = asIdentifiable(task.key.taskName, "TaskHandler")
): HandledTask<R>, Identifiable by identifiable{

    val actions : List<ActionSpan<*>> get()= task.actionSpans

    fun echo(message: String){
        dataProcessor.info(message)
    }
    fun <T2: PrintableBase<T2>> log(data: T2, template: PrintableTemplateBase<T2>)
        = dataProcessor.log<T2>(data, template)

    fun <T: Printable> logFormatted(data: T,   printFn: T.(StringBuilder)-> Unit)
        = dataProcessor.logFormatted(data, printFn)

    fun info(message: String): TaskData{
       return dataProcessor.info(message)
    }
    fun warn(message: String): TaskData{
        return dataProcessor.warn(message)
    }
    fun warn(ex: ManagedException, message: String = ""): TaskData{
        return dataProcessor.warn(ex, message)
    }

    fun subscribeHubEvents(
        eventType: NotifierHub.Event,
        callback: (PrintableBase<*>) -> Unit) = task.lookUpRoot().dispatcher.notifierHub.subscribe(this, eventType, callback)

    fun subscribeTaskEvents(handler: TaskDispatcher.UpdateType, callback: (LoggerStats) -> Unit) {
        task.callbackRegistry.subscribe(task, handler, wrapRawCallback(callback))
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

