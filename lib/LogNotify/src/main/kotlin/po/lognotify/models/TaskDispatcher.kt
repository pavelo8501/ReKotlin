package po.lognotify.models

import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskBase
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.interfaces.UpdatableTasks
import po.misc.callbacks.manager.CallbackContainer
import po.misc.callbacks.manager.Containable
import po.misc.callbacks.manager.callbackManager
import po.misc.callbacks.manager.withCallbackManager
import po.misc.callbacks.manager.withPayload
import po.misc.callbacks.manager.wrapRawCallback
import po.misc.coroutines.CoroutineInfo
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiableClass
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set


class TaskDispatcher(val notifierHub: NotifierHub) : UpdatableTasks, IdentifiableClass{

    enum class UpdateType{
        OnDataReceived,
        OnTaskCreated,
        OnTaskStart,
        OnTaskUpdated,
        OnTaskComplete
    }

    data class LoggerStats (
        val activeTask : ResultantTask<*, *>,
        val activeTaskName: String,
        val activeTaskNestingLevel: Int,
        val topTasksCount: Int,
        val totalTasksCount: Int,
        val coroutineInfo: CoroutineInfo,
    )

    override val identity = asIdentifiableClass("TaskDispatcher")


    internal val callbackRegistry = withCallbackManager<UpdateType> {
        withPayload<UpdateType, LoggerStats>(UpdateType.OnDataReceived){

        }
    }


    internal val taskHierarchy = ConcurrentHashMap<TaskKey, RootTask<*, *>>()
   // internal val callbackRegistry : MutableMap<UpdateType, (LoggerStats)-> Unit> = mutableMapOf()

    internal fun getTasks(): List<TaskBase<*,*>>{
        return taskHierarchy.values.toList()
    }

    fun onTaskCreated(handler: UpdateType, callback: (Containable<LoggerStats>) -> Unit) {
        callbackRegistry.subscribe<LoggerStats>(this, UpdateType.OnTaskCreated, callback)
    }

    fun onTaskComplete(handler: UpdateType, callback: (Containable<LoggerStats>) -> Unit) {
        callbackRegistry.subscribe<LoggerStats>(this, UpdateType.OnTaskComplete, callback)
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
        callbackRegistry.trigger(handler, stats)

    }
    fun addRootTask(task: RootTask<*, *>) {
        task.dataProcessor.config = notifierHub.config
        taskHierarchy[task.key] = task
        notifierHub.register(task)

        notifyUpdate(UpdateType.OnTaskCreated, task)
        notifyUpdate(UpdateType.OnTaskStart, task)
    }
    fun removeRootTask(task: RootTask<*, *>) {
        taskHierarchy.remove(task.key)
        notifierHub.unregister(task)
        notifyUpdate(UpdateType.OnTaskComplete, task)
    }
    fun activeRootTask(): RootTask<*, *>?{
      return taskHierarchy.values.firstOrNull { !it.isComplete }
    }


    fun keyLookup(name: String, nestingLevel: Int): TaskKey?{
        return taskHierarchy.keys.firstOrNull { it.taskName == name  && it.nestingLevel == nestingLevel}
    }
}