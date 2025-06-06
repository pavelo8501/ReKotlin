package po.lognotify.models

import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.interfaces.ResultantTask
import po.misc.types.UpdateType
import po.lognotify.classes.task.interfaces.UpdatableTasks
import po.misc.exceptions.CoroutineInfo
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set


class TaskDispatcher(val notifier: NotifierHub) : UpdatableTasks{

    data class LoggerStats (
        val activeTask : ResultantTask<*, *>,
        val activeTaskName: String,
        val activeTaskNestingLevel: Int,
        val topTasksCount: Int,
        val totalTasksCount: Int,
        val coroutineInfo: CoroutineInfo,

    )

    internal val taskHierarchy = ConcurrentHashMap<TaskKey, RootTask<*, *>>()
    internal val callbackRegistry : MutableMap<UpdateType, (LoggerStats)-> Unit> = mutableMapOf()

    fun onTaskCreated(handler: UpdateType, callback: (LoggerStats) -> Unit) {
        callbackRegistry[handler] = callback
    }

    fun onTaskComplete(handler: UpdateType, callback: (LoggerStats) -> Unit) {
        callbackRegistry[handler] = callback
    }

    override fun notifyUpdate(handler: UpdateType, task: ResultantTask<*, *>) {
        val stats = LoggerStats(
            activeTask = task,
            activeTaskName = task.key.taskName,
            activeTaskNestingLevel = task.key.nestingLevel,
            topTasksCount = taskHierarchy.size,
            totalTasksCount = taskHierarchy.values.sumOf { it.subTasksCount},
            coroutineInfo = task.coroutineInfo
        )
        callbackRegistry.filter { it.key == handler} .forEach { (_, cb) -> cb(stats) }
    }
    fun addRootTask(task: RootTask<*, *>, notifyConfig : NotifyConfig) {
        task.notifier.setNotifierConfig(notifyConfig)
        taskHierarchy[task.key] = task
        notifier.register(task.notifier)

        notifyUpdate(UpdateType.OnCreated, task)
        notifyUpdate(UpdateType.OnStart, task)
    }

    fun removeRootTask(task: RootTask<*, *>) {
        taskHierarchy.remove(task.key)
        notifier.unregister(task.notifier)
        notifyUpdate(UpdateType.OnComplete, task)
    }

    fun activeRootTask(): RootTask<*, *>?{
      return taskHierarchy.values.firstOrNull { !it.isComplete }
    }

    fun keyLookup(name: String, nestingLevel: Int): TaskKey?{
        return taskHierarchy.keys.firstOrNull { it.taskName == name  && it.nestingLevel == nestingLevel}
    }
}