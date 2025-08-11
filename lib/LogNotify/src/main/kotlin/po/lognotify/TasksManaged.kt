package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.models.LoggerStats
import po.lognotify.notification.NotifierHub
import po.lognotify.tasks.TaskHandler
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.UpdateType
import po.lognotify.notification.models.DebugData
import po.misc.callbacks.wrapRawCallback
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.context.CTX
import po.misc.data.logging.LogEmitter
import po.misc.data.printable.Printable
import po.misc.data.processors.SeverityLevel
import po.misc.debugging.DebugTopic
import kotlin.coroutines.CoroutineContext

interface TasksManaged : CTX {

    object LogNotify {
        val taskDispatcher: TaskDispatcher = TaskDispatcher(NotifierHub())
    }
    val logHandler: LogNotifyHandler
        get() = LogNotifyHandler(LogNotify.taskDispatcher)

    override val messageLogger: (String, SeverityLevel, Any) -> Unit get() = {message, severity, context->
        logHandler.logger.notify(message, severity, context)
    }
    override val datLogger: (PrintableBase<*>, SeverityLevel, Any) -> Unit get() = {printable, severity, context->
        logHandler.logger.log(printable, severity, context)
    }

    val taskHandler: TaskHandler<*>
        get() = LogNotify.taskDispatcher.activeTask()?.handler?: LogNotify.taskDispatcher.createDefaultTask().handler

    override fun <T: Printable> CTX.debug(message: String, template: PrintableTemplateBase<T>?, topic: DebugTopic){
        logHandler.logger.debug(message,  this, topic, template)
    }

    fun CTX.debug(message: String, template: PrintableTemplateBase<DebugData>?){
        logHandler.logger.debug(message,  this, DebugTopic.General, template)
    }


    fun <T : PrintableBase<T>> debug(data: T, dataClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>) {
        logHandler.dispatcher.getActiveDataProcessor().debug(data, dataClass, template)
    }

}

