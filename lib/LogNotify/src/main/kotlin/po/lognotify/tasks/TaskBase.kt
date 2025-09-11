package po.lognotify.tasks

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import po.lognotify.TasksManaged
import po.lognotify.action.ActionSpan
import po.lognotify.common.LNInstance
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.common.result.TaskResult
import po.lognotify.common.result.createFaultyResult
import po.lognotify.dispatcher.LoggerStats
import po.lognotify.models.SpanKey
import po.lognotify.dispatcher.TaskDispatcher
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
import po.misc.exceptions.ManagedException
import po.misc.reflection.classes.ClassInfo
import po.misc.time.ExecutionTimeStamp
import po.misc.time.startTimer
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
    val dispatcher: TaskDispatcher,
    override val receiver: T,
): ResultantTask<T, R>, LNInstance<T> {

    abstract var taskResult: TaskResult<T, R>?
    abstract val coroutineContext: CoroutineContext
    abstract val registry: TaskRegistry<*, *>
    abstract val callbackRegistry: CallbackManager<TaskDispatcher.UpdateType>
    abstract override val dataProcessor: LoggerDataProcessor
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(key.taskName, key.taskId.toString())
    abstract override val handler: TaskHandler<R>
    internal val actionSpans: MutableList<ActionSpan<*, *>> = mutableListOf()
    override val nestingLevel: Int get() = key.nestingLevel

    abstract override val header: String
    val footer: String get() = "$this  Completed in ${executionTimeStamp.elapsed}]"

    abstract fun start(): TaskBase<T, R>
    abstract override fun complete(): LNInstance<*>

    abstract override fun complete(exception: ManagedException)

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

    fun checkChildResult(childResult: TaskResult<*, *>): ManagedException? =
        childResult.exception?.let { exception ->
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

    fun collectException(throwable: Throwable){
        var result = taskResult
        if(result == null){
            result = TaskResult(this)
            taskResult = result
        }
        changeStatus(ExecutionStatus.Failing)
        result.collectThrowable(throwable)
    }
    fun <R> complete(result:R):R {
        changeStatus(ExecutionStatus.Complete)
        executionTimeStamp.stopTimer()
        dataProcessor.registerStop()
        if(this is RootTask){
            dispatcher.removeRootTask(this)
        }
        return result
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
    dispatcher: TaskDispatcher,
    override val scope: CoroutineScope,
    receiver: T,
) : TaskBase<T, R>(key, config, dispatcher, receiver), CoroutineHolder{

    override val identity: CTXIdentity<RootTask<T, R>> = asSubIdentity(this, receiver)

    override var coroutineContext: CoroutineContext  = scope.coroutineContext
        internal set

    override val dataProcessor: LoggerDataProcessor = LoggerDataProcessor(this, null)

    override var taskResult: TaskResult<T, R>? = null
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

    internal fun updateContext(context: CoroutineContext){

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
        changeStatus(ExecutionStatus.Complete)
        executionTimeStamp.stopTimer()
        dataProcessor.registerStop()
        dispatcher.removeRootTask(this)
        return this
    }

    override fun complete(exception: ManagedException){
        executionTimeStamp.stopTimer()
        registry.setChildTasksStatus(ExecutionStatus.Faulty, this)
        changeStatus(ExecutionStatus.Failing)
       // taskResult = createFaultyResult(exception, this)
        dataProcessor.registerStop()
        dispatcher.removeRootTask(this)
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
    override val dataProcessor: LoggerDataProcessor = LoggerDataProcessor(this, parentTask.dataProcessor)

    override val coroutineContext: CoroutineContext get() = hierarchyRoot.coroutineContext
    override var taskResult: TaskResult<T, R>? = null
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
        executionTimeStamp.startTimer()
        return this
    }

    override fun complete(): Task<T, R> {
        changeStatus(ExecutionStatus.Complete)
        executionTimeStamp.stopTimer()
        dataProcessor.registerStop()
        return this
    }

    override fun complete(exception: ManagedException) {
        executionTimeStamp.stopTimer()
        registry.setChildTasksStatus(ExecutionStatus.Faulty, this)
        changeStatus(ExecutionStatus.Failing)
       // taskResult = createFaultyResult(exception, this)
        dataProcessor.registerStop()
    }
}
