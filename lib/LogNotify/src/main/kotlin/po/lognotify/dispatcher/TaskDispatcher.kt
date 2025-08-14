package po.lognotify.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.lognotify.TasksManaged.LogNotify.taskDispatcher
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.models.TaskKey
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.NotifierHub
import po.lognotify.process.Process
import po.lognotify.process.ProcessKey
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.generateRootKey
import po.lognotify.tasks.warn
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.CoroutineInfo
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.functions.registries.builders.subscribe
import po.misc.functions.registries.builders.taggedRegistryOf
import po.misc.interfaces.Processable
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

    internal val notifier =  taggedRegistryOf<UpdateType, LoggerStats>()

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
        val task = createHierarchyRoot<TaskDispatcher, Unit>("Default", this, TaskConfig(isDefault = true))
        val warningMessage =
            """No active tasks in context, taskHandler() has created a default task to avoid crash.
        Make sure that logger tasks were started before calling this method.
            """.trimMargin()
        task.warn(warningMessage)
        return task
    }

//    @PublishedApi
//    internal suspend fun <T: TasksManaged, R> createRoot(
//        name: String,
//        receiver: T,
//        config: TaskConfig,
//    ): RootTask<T, R>{
//        val taskKey = generateRootKey(name, receiver)
//        val scope = assignCoroutineScope(name, config.launchOptions.coroutineDispatcher)
//        val newTask = RootTask<T, R>(taskKey, config, taskDispatcher, scope, receiver)
//        addRootTask(newTask)
//        notifier.trigger(UpdateType.OnTaskCreated, createLoggerStats(newTask, taskHierarchy))
//       return newTask
//    }

    @PublishedApi
    internal fun <T : CTX, R> createHierarchyRoot(
        name: String,
        receiver: T,
        config: TaskConfig,
    ): RootTask<T, R> {
        val taskKey = generateRootKey(name, receiver)
        val scope = assignCoroutineScope(name, config.launchOptions.coroutineDispatcher)
        val newTask = RootTask<T, R>(taskKey, config, taskDispatcher, scope, receiver)
        addRootTask(newTask)
        notifier.trigger(UpdateType.OnTaskCreated, createLoggerStats(newTask, taskHierarchy))
        return newTask
    }

    @PublishedApi
    internal fun registerProcess(process: Process<*>) {
        "Regestring process ${process.identifiedByName}".output(Colour.GREEN)
        subscribe(process.onComplete){
            "Closing process ${it.identifiedByName}".output(Colour.RED)
            processRegistry.remove(it.processKey)
        }
        processRegistry[process.processKey] = process
    }

    fun assignCoroutineScope(
        coroutineName: String,
        dispatcher: CoroutineDispatcher
    ): CoroutineScope{
        return activeProcess()?.scope ?:run {
            CoroutineScope(SupervisorJob() + dispatcher +  CoroutineName(coroutineName))
        }
    }

    fun onTaskUpdate(
        handler: UpdateType,
        callback: (LoggerStats) -> Unit,
    ) {
        notifier.subscribe(UpdateType.OnTaskCreated, this::class,  callback)
    }

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
    ): Process<T>? where  T: Processable{
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
