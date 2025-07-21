package po.lognotify.models

import po.lognotify.TasksManaged.LogNotify.defaultContext
import po.lognotify.TasksManaged.LogNotify.taskDispatcher
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.NotifierHub
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.interfaces.ResultantTask
import po.lognotify.tasks.interfaces.UpdatableTasks
import po.lognotify.tasks.models.TaskConfig
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.Containable
import po.misc.callbacks.builders.callbackManager
import po.misc.coroutines.CoroutineInfo
import po.misc.context.CTX
import po.misc.context.asIdentity
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set


class TaskDispatcher(val notifierHub: NotifierHub) : UpdatableTasks, CTX{

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

    override val identity = asIdentity()

    internal val callbackRegistry = callbackManager<UpdateType>(
        { CallbackManager.createPayload<UpdateType, LoggerStats>(this,  UpdateType.OnTaskCreated) },
        { CallbackManager.createPayload<UpdateType, LoggerStats>(this,  UpdateType.OnTaskStart) },
        { CallbackManager.createPayload<UpdateType, LoggerStats>(this,  UpdateType.OnTaskUpdated) },
        { CallbackManager.createPayload<UpdateType, LoggerStats>(this,  UpdateType.OnTaskComplete) }
    )
    init {
        notifierHub.hooks.debugListUpdated {debugWhiteList->
            notifierHub.sharedConfig.updateDebugWhiteList(debugWhiteList)
        }
    }

    fun getActiveDataProcessor(): LoggerDataProcessor{
        return activeTask()?.dataProcessor?:createDefaultTask().dataProcessor
    }

    internal fun createDefaultTask(): RootTask<TaskDispatcher, Unit> {
        val task = createHierarchyRoot<TaskDispatcher, Unit>("Default", "LogNotify", TaskConfig(), this)
        val warningMessage =
            """No active tasks in context, taskHandler() has created a default task to avoid crash.
        Make sure that logger tasks were started before calling this method.
        """.trimMargin()
        task.dataProcessor.warn(warningMessage)
        return task
    }


    @PublishedApi
    internal fun <T : CTX, R> createHierarchyRoot(
        name: String,
        moduleName: String,
        config: TaskConfig,
        receiver:T
    ): RootTask<T, R>{
        val newTask = RootTask<T, R>(TaskKey(name, 0, moduleName), config, defaultContext(name), taskDispatcher, receiver)
        taskDispatcher.addRootTask(newTask)
        return newTask
    }


    internal val taskHierarchy = ConcurrentHashMap<TaskKey, RootTask<*, *>>()
   // internal val callbackRegistry : MutableMap<UpdateType, (LoggerStats)-> Unit> = mutableMapOf()

    internal fun getTasks(): List<TaskBase<*, *>>{
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
    fun activeTask(): TaskBase<*, *>?{
        val activeRootTask = taskHierarchy.values.firstOrNull { it.taskStatus == ExecutionStatus.Active }
        return activeRootTask?.registry?.getActiveTask()
    }
    fun activeTasks(): List<TaskBase<*, *>>{
        return  taskHierarchy.values.filter { it.taskStatus == ExecutionStatus.Active }
    }

    fun keyLookup(name: String, nestingLevel: Int): TaskKey?{
        return taskHierarchy.keys.firstOrNull { it.taskName == name  && it.nestingLevel == nestingLevel}
    }
}