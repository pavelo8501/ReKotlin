package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.getOrLoggerException
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.UpdateType
import po.lognotify.models.TaskKey
import po.misc.callbacks.manager.wrapRawCallback
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplateBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableContext
import kotlin.coroutines.CoroutineContext

interface TasksManaged : IdentifiableContext {

    object LogNotify {
        val taskDispatcher: TaskDispatcher = TaskDispatcher(NotifierHub())
        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit =
            taskDispatcher.onTaskCreated(handler, wrapRawCallback(callback))

        fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit =
            taskDispatcher.onTaskComplete(handler, wrapRawCallback(callback))
    }

    val logHandler: LoggerDataProcessor
        get() {
            return taskHandler().dataProcessor
        }

    fun <T : PrintableBase<T>> log(data: T, template: PrintableTemplateBase<T>) {
        logHandler.log(data, template)
    }

    fun <T : PrintableBase<T>> debug(data: T, dataClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>) {
        logHandler.debug(data, dataClass, template)
    }

    fun taskHandler(): TaskHandler<*> {
        val activeTask = LogNotify.taskDispatcher.getActiveTasks()
        return activeTask.handler
    }

    fun logNotify(): LogNotifyHandler {
        return LogNotifyHandler(LogNotify.taskDispatcher)
    }
}

