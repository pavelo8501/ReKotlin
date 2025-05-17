package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.TasksManaged.Companion.taskDispatcher
import po.lognotify.classes.notification.ClassLevelNotifier
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.RootTaskSync
import po.lognotify.classes.task.TaskHandlerBase
import po.lognotify.classes.task.UpdateType
import po.lognotify.extensions.getOrLoggerException
import po.lognotify.logging.LoggingService
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskKey
import kotlin.coroutines.CoroutineContext

interface TasksManaged {

    companion object{

        val logger : LoggingService = LoggingService()
        val taskDispatcher: TaskDispatcher = TaskDispatcher()

        private val notifyConfig = NotifyConfig()
        val notifier : ClassLevelNotifier = ClassLevelNotifier(taskDispatcher, null)

        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskDispatcher.onTaskCreated(handler,callback)
        fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskDispatcher.onTaskComplete(handler, callback)

        private fun <R> hierarchyRootCreation(newRootTask :  RootTask<R>): RootTask<R>{
            taskDispatcher.finalizeAllTasks()
            taskDispatcher.addRootTask(newRootTask.key, newRootTask, notifier.getNotifierConfig())
            return newRootTask
        }

        @PublishedApi
        internal fun <R> createHierarchyRoot(name: String, moduleName: String): RootTask<R>{
            val newTask = RootTask<R>(TaskKey(name, 0, moduleName), defaultContext(name), taskDispatcher)
            return hierarchyRootCreation(newTask)
        }

        @PublishedApi
        internal fun <R> createHierarchyRootSynced(task : RootTaskSync<R>): RootTaskSync<R>{
            taskDispatcher.finalizeAllTasks()
            taskDispatcher.addRootTask(task.key, task, notifier.getNotifierConfig())
            return task
        }

        @PublishedApi
        internal fun <R> createHierarchyRoot(name: String, context: CoroutineContext, moduleName: String): RootTask<R> {
            val newTask = RootTask<R>(TaskKey(name, 0, moduleName), context, taskDispatcher)
            return  hierarchyRootCreation(newTask)
        }

//        internal fun <R> attachToHierarchy(name : String, moduleName: String?): SubTask<R>?{
//            val availableRoot = taskDispatcher.lastRootTask()
//            if(availableRoot != null){
//                val childTask = availableRoot.createNewMemberTask<R>(name, moduleName)
//                return childTask
//            }else{
//                return null
//            }
//        }

    }
}

fun  TasksManaged.logNotify(): LogNotifyHandler{
    return  LogNotifyHandler(TasksManaged.notifier)
}

fun  TasksManaged.lastTaskHandler(): TaskHandlerBase<*>{
    val availableRoot =  taskDispatcher.lastRootTask().getOrLoggerException("No available root task to continue on. All root tasks marked complete")
    return availableRoot.registry.getLastRegistered().taskHandler
}
