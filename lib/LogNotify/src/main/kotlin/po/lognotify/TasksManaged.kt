package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.enums.SeverityLevel
import po.lognotify.notification.NotifierHub
import po.lognotify.tasks.TaskHandler
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.UpdateType
import po.lognotify.notification.models.TaskData
import po.misc.callbacks.wrapRawCallback
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.companion.PrintableCompanion
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

    val taskHandler: TaskHandler<*> get(){
        return LogNotify.taskDispatcher.activeTask()?.handler?: LogNotify.taskDispatcher.createDefaultTask().handler
    }

    fun <T : PrintableBase<T>> log(data: T, template: PrintableTemplateBase<T>) {
        logHandler.dispatcher.getActiveDataProcessor().log(data, SeverityLevel.LOG)
    }


    fun notify(message: String, severity: SeverityLevel): TaskData{
       return logHandler.dispatcher.getActiveDataProcessor().notify(message, severity,  this)
    }

    fun <T : PrintableBase<T>> debug(data: T, dataClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>) {
        logHandler.dispatcher.getActiveDataProcessor().debug(data, dataClass, template)
    }

}

