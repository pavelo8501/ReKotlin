package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.TasksManaged.Companion.taskDispatcher
import po.lognotify.classes.notification.NotifierHub

import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.models.TaskConfig
import po.misc.types.UpdateType
import po.lognotify.extensions.getOrLoggerException
import po.lognotify.logging.LoggingService
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskKey
import kotlin.coroutines.CoroutineContext

interface TasksManaged {

    companion object{

        private val notifyConfig = NotifyConfig()
        val logger : LoggingService = LoggingService()
        val taskDispatcher: TaskDispatcher = TaskDispatcher(NotifierHub(null, notifyConfig))

        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskDispatcher.onTaskCreated(handler,callback)
        fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskDispatcher.onTaskComplete(handler, callback)

        @PublishedApi
        internal fun <R> createHierarchyRoot(name: String, moduleName: String, config: TaskConfig): RootTask<R>{
            val newTask = RootTask<R>(TaskKey(name, 0, moduleName), config, defaultContext(name), taskDispatcher)
            taskDispatcher.addRootTask(newTask, notifyConfig)
            return newTask
        }
    }
}

fun  TasksManaged.logNotify(): LogNotifyHandler{
    return  LogNotifyHandler(taskDispatcher)
}

fun  TasksManaged.lastTaskHandler(): TaskHandler<*>{
    val availableRoot =  taskDispatcher.activeRootTask().getOrLoggerException("No available root task to continue on. All root tasks marked complete")
    return availableRoot.registry.getLastChild()?.handler?:availableRoot.handler
}
