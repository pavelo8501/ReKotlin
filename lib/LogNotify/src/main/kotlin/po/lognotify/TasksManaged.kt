package po.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.classes.task.ManagedTask
import po.lognotify.classes.task.RootTask
import po.lognotify.exceptions.LoggerException
import po.lognotify.extensions.getOrThrow
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.safeCast
import po.lognotify.logging.LoggingService
import po.lognotify.models.TaskKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

interface TasksManaged {

    companion object {
        val logger = LoggingService()

        internal val taskHierarchy = ConcurrentHashMap<TaskKey, RootTask<*>>()

        internal fun defaultContext(name: String): CoroutineContext =
            SupervisorJob() + Dispatchers.Default + CoroutineName(name)

        private fun <R> hierarchyRootCreation(newRootTask :  RootTask<R>): RootTask<R>{
            taskHierarchy[newRootTask.key] = newRootTask
            return newRootTask
        }

        internal fun <R> createHierarchyRoot(name: String, moduleName: String?): RootTask<R>{
            val newTask = RootTask<R>(TaskKey(name, 0, moduleName), defaultContext(name))
            return hierarchyRootCreation(newTask)
        }

        internal fun <R> createHierarchyRoot(name: String, context: CoroutineContext, moduleName: String?): RootTask<R> {
            val newTask = RootTask<R>(TaskKey(name, 0, moduleName),context)
            return  hierarchyRootCreation(newTask)
        }

        internal fun <R> attachToHierarchy(name : String, moduleName: String?): ManagedTask<R>{
            val availableRoot = taskHierarchy.values.firstOrNull { it.isComplete == false }
                .getOrThrow(LoggerException("No available root task for sub task name:${name}|module:$moduleName. Bad setup"))
            val childTask = availableRoot.createNewMemberTask<R>(name, moduleName)
            return childTask
        }

        internal fun keyLookup(name: String, nestingLevel: Int): TaskKey?{
            return taskHierarchy.keys.firstOrNull { it.taskName == name  && it.nestingLevel == nestingLevel}
        }

       @PublishedApi
       internal fun <R> taskFromRegistry(key: TaskKey):ManagedTask<R>?{
           taskHierarchy.keys.firstOrNull { it.taskId == key.taskId  && it.nestingLevel == key.nestingLevel }?.let {
               taskHierarchy.values.firstOrNull().getOrThrowDefault("Task with name ${key.taskName} and FoldLevel ${key.nestingLevel} not found").let {
                    return it.safeCast<ManagedTask<R>>()
                }
            }
           return null
        }
    }

}