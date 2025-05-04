package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.task.ManagedTask
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.UpdateType
import po.lognotify.extensions.getOrLoggerException
import po.lognotify.logging.LoggingService
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskKey
import po.misc.exceptions.CoroutineInfo
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


interface TasksManaged  {

    companion object {

        val logger : LoggingService = LoggingService()
        val taskManager: TaskDispatcher = TaskDispatcher()
        val notifier : RootNotifier = RootNotifier(taskManager, null)

        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskManager.onTaskCreated(handler,callback)
        fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats) -> Unit): Unit
            = taskManager.onTaskComplete(handler, callback)

        private fun <R> hierarchyRootCreation(newRootTask :  RootTask<R>): RootTask<R>{
            taskManager.addRootTask(newRootTask.key, newRootTask, notifier.getNotifierConfig())
            return newRootTask
        }

        @PublishedApi
        internal fun <R> createHierarchyRoot(name: String, moduleName: String?): RootTask<R>{
            val newTask = RootTask<R>(TaskKey(name, 0, moduleName), defaultContext(name), taskManager)
            return hierarchyRootCreation(newTask)
        }

        @PublishedApi
        internal fun <R> createHierarchyRoot(name: String, context: CoroutineContext, moduleName: String?): RootTask<R> {
            val newTask = RootTask<R>(TaskKey(name, 0, moduleName), context, taskManager)
            return  hierarchyRootCreation(newTask)
        }

        internal fun <R> attachToHierarchy(name : String, moduleName: String?): ManagedTask<R>?{
            val availableRoot = taskManager.lastRootTask()
            if(availableRoot != null){
                val childTask = availableRoot.createNewMemberTask<R>(name, moduleName)
                return childTask
            }else{
                return null
            }
        }

        internal fun getLastTaskHandler(): TaskHandler<*>{
            val availableRoot =  taskManager.lastRootTask().getOrLoggerException("No available root task to continue on. All root tasks marked complete")
            return availableRoot.registry.getLastRegistered().taskHandler
        }
    }

}