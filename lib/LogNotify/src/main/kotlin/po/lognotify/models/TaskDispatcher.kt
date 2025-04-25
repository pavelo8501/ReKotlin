package po.lognotify.models

import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.UpdatableTasks
import po.lognotify.classes.task.UpdateType
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set


class TaskDispatcher() : UpdatableTasks{

    data class LoggerStats (val topTasksCount: Int, val totalTasksCount: Int)

    internal val taskHierarchy = ConcurrentHashMap<TaskKey, RootTask<*>>()
    internal val callbackRegistry : MutableMap<UpdateType, (LoggerStats)-> Unit> = mutableMapOf()

    override fun onTaskCreated(handler: UpdateType, callback: (LoggerStats) -> Unit) {
        callbackRegistry[handler] = callback
    }
    override fun onTaskComplete(handler: UpdateType, callback: (LoggerStats) -> Unit) {
        callbackRegistry[handler] = callback
    }
    override fun notifyUpdate(handler: UpdateType) {
        val stats = LoggerStats(
            topTasksCount = taskHierarchy.size,
            totalTasksCount = taskHierarchy.values.sumOf { it.subTasksCount() }
        )
        callbackRegistry.filter { it.key == handler} .forEach { (_, cb) -> cb(stats) }
    }
    fun addRootTask(key: TaskKey, task: RootTask<*>) {
        taskHierarchy[key] = task
        notifyUpdate(UpdateType.OnStart)
    }

    fun removeRootTask(task: RootTask<*>) {
        taskHierarchy.remove(task.key)
        notifyUpdate(UpdateType.OnStart)
    }

    fun removeRootTask(key: TaskKey) {
        taskHierarchy.remove(key)
        notifyUpdate(UpdateType.OnStart)
    }
    fun lastRootTask(): RootTask<*>?{
        return  taskHierarchy.values.firstOrNull {!it.isComplete}
    }
    fun keyLookup(name: String, nestingLevel: Int): TaskKey?{
        return taskHierarchy.keys.firstOrNull { it.taskName == name  && it.nestingLevel == nestingLevel}
    }
}