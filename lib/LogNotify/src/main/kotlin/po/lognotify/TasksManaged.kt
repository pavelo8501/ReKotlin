package po.lognotify

import po.lognotify.notification.NotifierHub
import po.lognotify.tasks.TaskHandler
import po.lognotify.dispatcher.TaskDispatcher
import po.lognotify.notification.models.DebugData
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.context.CTX
import po.misc.data.printable.Printable
import po.misc.data.processors.SeverityLevel
import po.misc.debugging.DebugTopic

interface TasksManaged : CTX {

    object LogNotify {
       @PublishedApi
       internal val taskDispatcher: TaskDispatcher = TaskDispatcher(NotifierHub())
    }
    val logHandler: LogNotifyHandler
        get() = LogNotifyHandler(LogNotify.taskDispatcher)

    val taskHandler: TaskHandler<*>
        get() = LogNotify.taskDispatcher.activeTask()?.handler?: LogNotify.taskDispatcher.createDefaultTask().handler

    override fun Any.log(data: PrintableBase<*>, severity: SeverityLevel){
        logHandler.logger.log(data,  severity, this@log)
    }

    override fun Any.notify(message: String, severity: SeverityLevel){
        logHandler.logger.notify(this@notify,  message,  severity)
    }

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

