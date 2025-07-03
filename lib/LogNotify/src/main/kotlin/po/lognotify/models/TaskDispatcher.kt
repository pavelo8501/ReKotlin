package po.lognotify.models

import po.lognotify.TasksManaged.LogNotify.defaultContext
import po.lognotify.TasksManaged.LogNotify.taskDispatcher
import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskBase
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.interfaces.UpdatableTasks
import po.lognotify.classes.task.models.TaskConfig
import po.misc.callbacks.manager.CallbackManager
import po.misc.callbacks.manager.Containable
import po.misc.callbacks.manager.builders.callbackManager
import po.misc.callbacks.manager.builders.withCallbackManager
import po.misc.coroutines.CoroutineInfo
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.asIdentifiableClass
import java.util.concurrent.ConcurrentHashMap
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

    override val identity = asIdentifiableClass("LogNotify", "TaskDispatcher")

    internal val callbackRegistry = callbackManager<UpdateType>(
        { CallbackManager.createPayload<UpdateType, LoggerStats>(it,  UpdateType.OnTaskCreated) },
        { CallbackManager.createPayload<UpdateType, LoggerStats>(it,  UpdateType.OnTaskStart) },
        { CallbackManager.createPayload<UpdateType, LoggerStats>(it,  UpdateType.OnTaskUpdated) },
        { CallbackManager.createPayload<UpdateType, LoggerStats>(it,  UpdateType.OnTaskComplete) }
    )
    init {
        notifierHub.hooks.debugListUpdated {debugWhiteList->
            notifierHub.sharedConfig.updateDebugWhiteList(debugWhiteList)
        }
    }

    private fun createDefaultTask(): RootTask<TaskDispatcher, Unit> {
        val task = createHierarchyRoot<TaskDispatcher, Unit>("Default", "LogNotify", TaskConfig(), this)
        val warningMessage =
            """No active tasks in context, taskHandler() has created a default task to avoid crash.
        Make sure that logger tasks were started before calling this method.
        """.trimMargin()
        task.dataProcessor.warn(warningMessage)
        return task
    }

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


    internal val taskHierarchy = ConcurrentHashMap<TaskKey, RootTask<*, *>>()
   // internal val callbackRegistry : MutableMap<UpdateType, (LoggerStats)-> Unit> = mutableMapOf()

    internal fun getTasks(): List<TaskBase<*, *>>{
        return taskHierarchy.values.toList()
    }

    internal fun getActiveTasks(): TaskBase<*, *>{
       val activeRootTask =  taskHierarchy.values.firstOrNull { it.taskStatus == TaskBase.TaskStatus.Active }
       return activeRootTask?.registry?.getActiveTask() ?: createDefaultTask()
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

    fun activeRootTasks(): List<RootTask<*, *>>{
        return taskHierarchy.values.filter {!it.isComplete }
    }

    fun keyLookup(name: String, nestingLevel: Int): TaskKey?{
        return taskHierarchy.keys.firstOrNull { it.taskName == name  && it.nestingLevel == nestingLevel}
    }
}