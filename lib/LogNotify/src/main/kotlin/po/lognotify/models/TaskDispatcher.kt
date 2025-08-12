package po.lognotify.models

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.TasksManaged
import po.lognotify.TasksManaged.LogNotify.taskDispatcher
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.NotifierHub
import po.lognotify.process.Process
import po.lognotify.process.ProcessKey
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.warn
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.CoroutineInfo
import po.misc.data.logging.LogCollector
import po.misc.functions.registries.emitterAwareRegistryOf
import po.misc.types.safeCast
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

data class LoggerStats(
    val activeTask: TaskBase<*, *>,
    val activeTaskName: String,
    val activeTaskNestingLevel: Int,
    val topTasksCount: Int,
    val totalTasksCount: Int,
    val coroutineInfo: CoroutineInfo,
)

class TaskDispatcher(val notifierHub: NotifierHub): CTX {

    enum class UpdateType {
        OnDataReceived,
        OnTaskCreated,
        OnTaskStart,
        OnTaskUpdated,
        OnTaskComplete,
    }


    override val identity: CTXIdentity<TaskDispatcher> = asIdentity()

    internal val notifier =  emitterAwareRegistryOf<TaskDispatcher, UpdateType, LoggerStats>()

    internal val taskHierarchy = ConcurrentHashMap<TaskKey, RootTask<*, *>>()
    internal val processRegistry = ConcurrentHashMap<ProcessKey<*>, Process<*>>()
    internal fun getTasks(): List<TaskBase<*, *>> = taskHierarchy.values.toList()


    private fun createLoggerStats(
        task: TaskBase<*, *>,
        taskHierarchy:  ConcurrentHashMap<TaskKey, RootTask<*, *>>
    ):LoggerStats{
        return LoggerStats(
            activeTask = task,
            activeTaskName = task.key.taskName,
            activeTaskNestingLevel = task.key.nestingLevel,
            topTasksCount = taskHierarchy.size,
            totalTasksCount = taskHierarchy.values.sumOf { it.registry.totalCount },
            coroutineInfo = task.coroutineInfo,
        )
    }

    fun getActiveDataProcessor(): LoggerDataProcessor = activeTask()?.dataProcessor ?: createDefaultTask().dataProcessor

    internal fun defaultContext(name: String): CoroutineContext =
        SupervisorJob() + Dispatchers.Default + CoroutineName(name)

    internal fun createDefaultTask(): RootTask<TaskDispatcher, Unit> {
        val task = createHierarchyRoot<TaskDispatcher, Unit>("Default", "LogNotify", this, TaskConfig(isDefault = true))
        val warningMessage =
            """No active tasks in context, taskHandler() has created a default task to avoid crash.
        Make sure that logger tasks were started before calling this method.
            """.trimMargin()
        task.warn(warningMessage)
        return task
    }

    @PublishedApi
    internal suspend fun <T: TasksManaged, R> createRoot(
        name: String,
        moduleName: String,
        receiver: T,
        config: TaskConfig = TaskConfig(isDefault = true),
    ): RootTask<T, R>{


        val process = processRegistry.values.firstOrNull()
        val newTask = RootTask<T, R>(TaskKey(name, 0, moduleName), config, defaultContext(name), taskDispatcher, receiver)
        if(process != null){
            newTask.updateContext(process.coroutineContext)
        }
        addRootTask(newTask)
        notifier.trigger(UpdateType.OnTaskCreated, createLoggerStats(newTask, taskHierarchy))
       return newTask
    }

    @PublishedApi
    internal fun <T : CTX, R> createHierarchyRoot(
        name: String,
        moduleName: String,
        receiver: T,
        config: TaskConfig = TaskConfig(isDefault = true),
    ): RootTask<T, R> {

        val newTask = RootTask<T, R>(TaskKey(name, 0, moduleName), config, defaultContext(name), taskDispatcher, receiver)
        val process = processRegistry.values.firstOrNull()
        if(process != null){
            newTask.updateContext(process.coroutineContext)
        }
        addRootTask(newTask)
        return newTask
    }

    @PublishedApi
    internal fun <T : CTX, R> createHierarchyRoot(
        name: String,
        receiver: T,
        config: TaskConfig = TaskConfig(isDefault = true),
    ): RootTask<T, R> {
        val newTask =
            RootTask<T, R>(TaskKey(name, 0, receiver.identity.identifiedByName), config, defaultContext(name), taskDispatcher, receiver)
        addRootTask(newTask)

        return newTask
    }

    fun registerProcess(process: Process<*>) {
        processRegistry[process.processKey] = process
    }

    fun onTaskUpdate(
        handler: UpdateType,
        callback: (LoggerStats) -> Unit,
    ) {
        notifier.subscribe(UpdateType.OnTaskCreated, this::class,  callback)
    }

//    fun onTaskComplete(
//        handler: UpdateType,
//        callback: (Containable<LoggerStats>) -> Unit,
//    ) {
//        notifier.subscribe(UpdateType.OnTaskComplete,  this,  callback)
//    }


    fun notifyUpdate(
        handler: UpdateType,
        task: TaskBase<*, *>,
    ) {
        val stats = createLoggerStats(task, taskHierarchy)
        notifier.trigger(handler, stats)
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

    fun activeRootTask(): RootTask<*, *>? = taskHierarchy.values.firstOrNull { !it.isComplete }

    fun activeTask(): TaskBase<*, *>? {
        val activeRootTask = taskHierarchy.values.firstOrNull { it.executionStatus == ExecutionStatus.Active }
        return activeRootTask?.registry?.getActiveTask()
    }

    fun <T> lookUpProcess(
        processKey: ProcessKey<T>
    ): Process<T>? where  T: CTX, T: LogCollector, T: CoroutineContext.Element{
        val process = processRegistry[processKey]
        return process?.safeCast<Process<T>>()
    }

    fun activeProcess(): Process<*>?{
        return processRegistry.values.firstOrNull()
    }


    fun activeTasks(): List<TaskBase<*, *>> = taskHierarchy.values.filter { it.executionStatus == ExecutionStatus.Active }

    fun keyLookup(
        name: String,
        nestingLevel: Int,
    ): TaskKey? =
        taskHierarchy.keys.firstOrNull {
            it.taskName == name && it.nestingLevel == nestingLevel
        }
}
