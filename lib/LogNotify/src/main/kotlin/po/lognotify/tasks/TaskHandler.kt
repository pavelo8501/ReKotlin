package po.lognotify.tasks

import po.lognotify.action.ActionSpan
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.tasks.interfaces.HandledTask
import po.misc.callbacks.wrapRawCallback
import po.misc.context.CTX
import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.processors.SeverityLevel

class TaskHandler<R : Any?>(
    val task: TaskBase<*, R>,
    internal val dataProcessor: LoggerDataProcessor,
) : HandledTask<R> {
    val actions: List<ActionSpan<*, *>> get() = task.actionSpans

    val taskConfig: TaskConfig get() = task.config

    fun <T2 : PrintableBase<T2>> CTX.log(
        data: T2,
        severity: SeverityLevel = SeverityLevel.INFO,
    ) = dataProcessor.log(data, severity, this)

    fun CTX.notify(
        message: String,
        severity: SeverityLevel = SeverityLevel.INFO,
    ) = dataProcessor.notify(message, severity, this)

    fun <T2 : PrintableBase<T2>> debug(
        data: T2,
        dataClass: PrintableCompanion<T2>,
        template: PrintableTemplateBase<T2>,
    ): T2 = dataProcessor.debug(data, dataClass, template)

    fun <T : Printable> logFormatted(
        data: T,
        printFn: T.(StringBuilder) -> Unit,
    ) = dataProcessor.logFormatted(data, printFn)

    fun subscribeTaskEvents(
        handler: TaskDispatcher.UpdateType,
        callback: (LoggerStats) -> Unit,
    ) {
        task.callbackRegistry.subscribe(task, handler, wrapRawCallback(callback))
    }
}
