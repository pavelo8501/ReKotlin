package po.lognotify.tasks

import po.lognotify.action.ActionSpan
import po.lognotify.enums.SeverityLevel
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.models.TaskData
import po.lognotify.tasks.interfaces.HandledTask
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.misc.callbacks.wrapRawCallback
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.Printable
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.exceptions.ManagedException

class TaskHandler<R: Any?>(
    val task : TaskBase<*, R>,
    internal val dataProcessor: LoggerDataProcessor,
): HandledTask<R>{

    val actions : List<ActionSpan<*, *>> get()= task.actionSpans

    val taskConfig: TaskConfig get() = task.config

    fun echo(message: String){
        dataProcessor.info(message)
    }

    fun <T2: PrintableBase<T2>> log(data: T2):T2 = dataProcessor.log(data, SeverityLevel.LOG)


    fun <T2: PrintableBase<T2>> debug(data: T2, dataClass: PrintableCompanion<T2>, template: PrintableTemplateBase<T2>):T2
        = dataProcessor.debug(data, dataClass, template)


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

//    fun subscribeHubEvents(
//        eventType: NotifierHub.Event,
//        callback: (PrintableBase<*>) -> Unit) = task.lookUpRoot().dispatcher.notifierHub.subscribe(this, eventType, callback)

    fun subscribeTaskEvents(handler: TaskDispatcher.UpdateType, callback: (LoggerStats) -> Unit) {
        task.callbackRegistry.subscribe(task, handler, wrapRawCallback(callback))
    }
}

