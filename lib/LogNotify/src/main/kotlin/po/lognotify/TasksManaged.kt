package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.notification.NotifierHub
import po.lognotify.tasks.TaskHandler
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.UpdateType
import po.misc.callbacks.wrapRawCallback
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.context.CTX
import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import kotlin.coroutines.CoroutineContext

interface TasksManaged : CTX, LogEmitter {

    object LogNotify {
        val taskDispatcher: TaskDispatcher = TaskDispatcher(NotifierHub())
        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit =
            taskDispatcher.onTaskCreated(handler, wrapRawCallback(callback))

        fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit =
            taskDispatcher.onTaskComplete(handler, wrapRawCallback(callback))
    }

    override val datLogger: (PrintableBase<*>, SeverityLevel, Any) -> Unit get() = logHandler.logger::log
    override val messageLogger: (String, SeverityLevel, Any) -> Unit get() = logHandler.logger::notify

    val logHandler: LogNotifyHandler
        get() = LogNotifyHandler(LogNotify.taskDispatcher)


    val taskHandler: TaskHandler<*>
        get() = LogNotify.taskDispatcher.activeTask()?.handler?: LogNotify.taskDispatcher.createDefaultTask().handler


//    override fun <T : PrintableBase<T>> CTX.log(data: T, severity: SeverityLevel) {
//        logHandler.dispatcher.getActiveDataProcessor().log(data, severity, this@log)
//    }
//
//    fun CTX.notify(message: String, severity: SeverityLevel = SeverityLevel.INFO){
//        logHandler.logger.notify(message, severity, this@notify)
//    }

    fun <T : PrintableBase<T>> debug(data: T, dataClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>) {
        logHandler.dispatcher.getActiveDataProcessor().debug(data, dataClass, template)
    }

}

