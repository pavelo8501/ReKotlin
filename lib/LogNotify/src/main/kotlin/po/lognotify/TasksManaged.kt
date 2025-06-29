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
import po.misc.exceptions.throwManaged
import po.misc.interfaces.IdentifiableContext
import kotlin.coroutines.CoroutineContext


interface LoggableContext: IdentifiableContext{

//    val logHandler:  LoggerDataProcessor get() {
//      return TasksManaged.LogNotify.taskDispatcher.activeRootTask()?.dataProcessor?:run {
//          throwManaged("dataProcessor not found")
//      }
//    }

//    fun<T: PrintableBase<T>> log(record: T, template: PrintableTemplateBase<T>): T{
//        logHandler.log(record, template)
//        return record
//    }
//
//    fun<T: PrintableBase<T>> debug(data: T, dataClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>): T{
//        return  logHandler.debug(data,  dataClass, template)
//    }
//
//    fun<T: PrintableBase<T>> info(record:  T, template: PrintableTemplateBase<T>){
//        logHandler.log(record, template)
//    }
}


interface TasksManaged :  LoggableContext {

    object LogNotify {
        val taskDispatcher: TaskDispatcher = TaskDispatcher(NotifierHub())
        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskDispatcher.onTaskCreated(handler, wrapRawCallback(callback))
        fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskDispatcher.onTaskComplete(handler, wrapRawCallback(callback))

        @PublishedApi
        internal fun <T, R> createHierarchyRoot(
            name: String,
            moduleName: String,
            config: TaskConfig,
            receiver:T
        ): RootTask<T, R>
        {
            val newTask = RootTask<T, R>(TaskKey(name, 0, moduleName), config, defaultContext(name), taskDispatcher, receiver)
            taskDispatcher.addRootTask(newTask)
            return newTask
        }
    }


    val logHandler:  LoggerDataProcessor? get() {
      return  taskHandlerSafe()?.dataProcessor
    }

    fun<T: PrintableBase<T>> log(data: T, template: PrintableTemplateBase<T>){
        logHandler?.log(data, template)?:run {
            data.echo(template)
        }
    }

    fun<T: PrintableBase<T>> debug(data: T, dataClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>){
        logHandler?.debug(data,  dataClass, template)?:run {
            data.echo(template)
        }
    }

//    fun<T: PrintableBase<T>> info(record:  T, template: PrintableTemplateBase<T>){
//        logHandler?.log(record, template)
//    }


    fun taskHandlerSafe(): TaskHandler<*>?{
        val message = """taskHandler() resulted in failure. Unable to get task handler. No active tasks in context.
        Make sure that logger tasks were started before calling this method.
    """.trimMargin()

        val availableRoot =  LogNotify.taskDispatcher.activeRootTask()
        return availableRoot?.registry?.getLastSubTask()?.handler?:availableRoot?.handler
    }

   fun taskHandler(): TaskHandler<*>{
        val message = """taskHandler() resulted in failure. Unable to get task handler. No active tasks in context.
        Make sure that logger tasks were started before calling this method.
    """.trimMargin()

        val availableRoot =  LogNotify.taskDispatcher.activeRootTask().getOrLoggerException(message)
        return availableRoot.registry.getLastSubTask()?.handler?:availableRoot.handler
    }

    fun logNotify(): LogNotifyHandler{
        return  LogNotifyHandler(LogNotify.taskDispatcher)
    }
//
//    fun lastTaskHandler(): TaskHandler<*>{
//        val message = """lastTaskHandler() resulted in failure. Unable to get task handler. No active tasks in context.
//        Make sure that logger tasks were started before calling this method.
//    """.trimMargin()
//
//        val availableRoot =  TasksManaged.LogNotify.taskDispatcher.activeRootTask().getOrLoggerException(message)
//        return availableRoot.registry.getLastSubTask()?.handler?:availableRoot.handler
//    }
}

