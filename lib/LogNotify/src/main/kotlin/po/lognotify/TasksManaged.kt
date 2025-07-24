package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.enums.SeverityLevel
import po.lognotify.notification.NotifierHub
import po.lognotify.tasks.TaskHandler
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.UpdateType
import po.misc.callbacks.wrapRawCallback
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableTemplateBase
import po.misc.data.printable.PrintableCompanion
import po.misc.context.CTX
import kotlin.coroutines.CoroutineContext

interface TasksManaged : CTX {

    object LogNotify {
        val taskDispatcher: TaskDispatcher = TaskDispatcher(NotifierHub())
        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit =
            taskDispatcher.onTaskCreated(handler, wrapRawCallback(callback))

        fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit =
            taskDispatcher.onTaskComplete(handler, wrapRawCallback(callback))
    }

    val logHandler: LogNotifyHandler
        get() {
            return LogNotifyHandler(LogNotify.taskDispatcher)
        }

    fun <T : PrintableBase<T>> log(data: T, template: PrintableTemplateBase<T>) {
        logHandler.dispatcher.getActiveDataProcessor().log(data, SeverityLevel.LOG)
    }



    fun <T : PrintableBase<T>> debug(data: T, dataClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>) {
        logHandler.dispatcher.getActiveDataProcessor().debug(data, dataClass, template)
    }

    fun taskHandler(): TaskHandler<*> {
        return LogNotify.taskDispatcher.activeTask()?.handler?: LogNotify.taskDispatcher.createDefaultTask().handler
    }
}

