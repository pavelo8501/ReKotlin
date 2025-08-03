package po.lognotify.tasks

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import po.lognotify.TasksManaged
import po.lognotify.action.ActionSpan
import po.lognotify.common.LNInstance
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.common.result.TaskResult
import po.lognotify.common.result.createFaultyResult
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.SpanKey
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.models.ActionData
import po.lognotify.notification.models.ErrorSnapshot
import po.lognotify.notification.models.FailureReasoning
import po.lognotify.tasks.interfaces.ResultantTask
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.builders.callbackManager
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.coroutines.CoroutineHolder
import po.misc.coroutines.CoroutineInfo
import po.misc.data.processors.FlowEmitter
import po.misc.exceptions.ManagedException
import po.misc.reflection.classes.ClassInfo
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.startTimer
import po.misc.time.stopTimer
import kotlin.coroutines.CoroutineContext

enum class ExecutionStatus {
    Active,
    Complete,
    Failing,
    Faulty,
}

sealed class TaskBase<T : CTX, R : Any?>(
    override val key: TaskKey,
    override val config: TaskConfig,
    dispatcher: TaskDispatcher,
    override val receiver: T,
) : StaticHelper,
    MeasuredContext,
    ResultantTask<T, R>,
    LNInstance<T> {
    abstract var taskResult: TaskResult<R>?
    abstract val coroutineContext: CoroutineContext
    abstract val registry: TaskRegistry<*, *>
    abstract val callbackRegistry: CallbackManager<TaskDispatcher.UpdateType>
    abstract override val dataProcessor: LoggerDataProcessor
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(key.taskName, key.taskId.toString())
    abstract override val handler: TaskHandler<R>
    internal val actionSpans: MutableList<ActionSpan<*, *>> = mutableListOf()
    override val nestingLevel: Int get() = key.nestingLevel

    abstract override val header: String
    val footer: String get() = "Completed in ${executionTimeStamp.elapsed}"

    abstract fun start(): TaskBase<T, R>

    abstract override fun complete(): LNInstance<*>

    abstract override fun complete(exception: ManagedException): Nothing

    protected var taskStatus: ExecutionStatus = ExecutionStatus.Active
    override val executionStatus: ExecutionStatus get() = taskStatus

    internal fun lookUpRoot(): RootTask<*, *> = registry.hierarchyRoot

    fun notifyUpdate(handler: TaskDispatcher.UpdateType) {
        val stats =
            LoggerStats(
                activeTask = this,
                activeTaskName = this.key.taskName,
                activeTaskNestingLevel = this.key.nestingLevel,
                topTasksCount = 1,
                totalTasksCount = registry.tasks.count(),
                coroutineInfo = CoroutineInfo.createInfo(coroutineContext),
            )
        callbackRegistry.trigger<LoggerStats>(handler, stats)
    }

    override fun changeStatus(status: ExecutionStatus) {
        taskStatus = status
    }

    fun checkChildResult(childResult: TaskResult<*>): ManagedException? =
        childResult.throwable?.let { exception ->
            val childTask = childResult.task
            childTask.taskStatus = ExecutionStatus.Faulty
            exception
        }

    fun <T2 : TasksManaged, R2 : Any?> createActionSpan(
        name: String,
        receiver: T2,
    ): ActionSpan<T2, R2> {
        val spanKey = SpanKey(name, actionSpans.size + 1, this@TaskBase.key.taskName, this@TaskBase.key.nestingLevel)
        val actionSpan = ActionSpan<T2, R2>(name, spanKey, receiver, this)
        actionSpans.add(actionSpan)
        return actionSpan
    }

    fun activeActionSpan(): ActionSpan<*, *>? = actionSpans.firstOrNull { it.executionStatus == ExecutionStatus.Active }

    fun checkIfCanBeSkipped(classInfo: ClassInfo<R>?): Boolean {
        if (classInfo == null) {
            return false
        }
        if (classInfo.acceptsNull) {
            return true
        } else {
            return false
        }
    }

    fun createTaskData(builder: TaskBase<*, *>.() -> List<ActionData>): ErrorSnapshot {
        val result = this.builder()
        val snapshot =
            ErrorSnapshot(
                taskHeader = header,
                taskStatus = executionStatus,
            )
        snapshot.actionRecords = result
        return snapshot
    }

    override fun toString(): String =
        when (this) {
            is RootTask -> "(R) ${key.taskName}"
            is Task -> "(${key.nestingLevel}) ${key.taskName}"
        }
}

class RootTask<T : CTX, R : Any?>(
    key: TaskKey,
    config: TaskConfig,
    override val coroutineContext: CoroutineContext,
    val dispatcher: TaskDispatcher,
    receiver: T,
) : TaskBase<T, R>(key, config, dispatcher, receiver),
    CoroutineHolder {
    override val identity: CTXIdentity<RootTask<T, R>> = asSubIdentity(this, receiver)

    override val dataProcessor: LoggerDataProcessor = LoggerDataProcessor(this, null, FlowEmitter())

    override var taskResult: TaskResult<R>? = null
    override val registry: TaskRegistry<T, R> = TaskRegistry(dispatcher, this)
    override val callbackRegistry = callbackManager<TaskDispatcher.UpdateType>()
    override val handler: TaskHandler<R> = TaskHandler(this, dataProcessor)
    override val rootTask: RootTask<*, *> get() = this
    override val header: String get() = "$this | Module: ${key.moduleName}"

    var isComplete: Boolean = false
    override val coroutineInfo: CoroutineInfo = CoroutineInfo.createInfo(coroutineContext)
    var escalationCallback: ((ManagedException) -> Unit)? = null
    val failureReasoning: MutableList<FailureReasoning> = mutableListOf()

    fun saveFailureReasoning(reason: FailureReasoning) {
        failureReasoning.add(reason)
    }

    internal fun commitSuicide(exception: ManagedException?) {
        val cancellation =
            exception?.let {
                CancellationException(it.message, exception)
            }
        val job = coroutineContext[Job]
        if (job != null) {
            dataProcessor.debug("Cancelling Job $job", "RootTask(commitSuicide)")
            job.cancel(cancellation)
        } else {
            dataProcessor.debug("Cancelling Context ${coroutineContext[CoroutineName]?.name ?: "Unknown"}", "RootTask(commitSuicide)")
            coroutineContext.cancel(cancellation)
        }
    }

    override fun start(): TaskBase<T, R> {
        startTimer()
        return this
    }

    fun start(onEscalation: ((ManagedException) -> Unit)? = null): RootTask<T, R> {
        escalationCallback = onEscalation
        startTimer()
        return this
    }

    fun onChildCreated(childTask: Task<*, *>) {
        notifyUpdate(TaskDispatcher.UpdateType.OnTaskUpdated)
        registry.registerChild(childTask)
        dispatcher.notifyUpdate(TaskDispatcher.UpdateType.OnTaskUpdated, this)
    }

    override fun complete(): LNInstance<*> {
        stopTimer()
        isComplete = true
        dataProcessor.registerStop()
        dispatcher.removeRootTask(this)
        return this
    }

    override fun complete(exception: ManagedException): Nothing {
        stopTimer()
        registry.setChildTasksStatus(ExecutionStatus.Faulty, this)
        changeStatus(ExecutionStatus.Failing)
        taskResult = createFaultyResult(exception, this)
        isComplete = true
        dataProcessor.registerStop()
        dispatcher.removeRootTask(this)
        throw exception
    }
}

class Task<T : CTX, R : Any?>(
    key: TaskKey,
    config: TaskConfig,
    internal val parentTask: TaskBase<*, *>,
    internal val hierarchyRoot: RootTask<*, *>,
    receiver: T,
) : TaskBase<T, R>(key, config, hierarchyRoot.dispatcher, receiver),
    ResultantTask<T, R> {
    override val identity: CTXIdentity<Task<T, R>> = asSubIdentity(this, receiver)
    override val dataProcessor: LoggerDataProcessor = LoggerDataProcessor(this, parentTask.dataProcessor, null)

    override val coroutineContext: CoroutineContext get() = hierarchyRoot.coroutineContext
    override var taskResult: TaskResult<R>? = null
    override val registry: TaskRegistry<*, *> get() = hierarchyRoot.registry
    override val callbackRegistry = callbackManager<TaskDispatcher.UpdateType>()
    override val handler: TaskHandler<R> = TaskHandler<R>(this, dataProcessor)
    override val coroutineInfo: CoroutineInfo = CoroutineInfo.createInfo(coroutineContext)
    override val rootTask: RootTask<*, *> get() = hierarchyRoot
    override val header: String get() = "$this | Module: ${key.moduleName}"

    fun notifyRootCancellation(exception: ManagedException?) {
        hierarchyRoot.commitSuicide(exception)
    }

    override fun start(): Task<T, R> {
        startTimer()
        return this
    }

    override fun complete(): Task<T, R> {
        stopTimer()
        dataProcessor.registerStop()
        return this
    }

    override fun complete(exception: ManagedException): Nothing {
        stopTimer()
        registry.setChildTasksStatus(ExecutionStatus.Faulty, this)
        changeStatus(ExecutionStatus.Failing)
        taskResult = createFaultyResult(exception, this)
        dataProcessor.registerStop()
        throw exception
    }
}
