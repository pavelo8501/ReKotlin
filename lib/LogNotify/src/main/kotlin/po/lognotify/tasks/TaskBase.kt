package po.lognotify.tasks

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import po.lognotify.classes.notification.models.ActionData
import po.lognotify.action.ActionSpan
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.models.ErrorSnapshot
import po.lognotify.common.LogInstance
import po.lognotify.tasks.interfaces.ResultantTask
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.common.result.TaskResult
import po.lognotify.common.result.createFaultyResult
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.builders.callbackManager
import po.misc.coroutines.CoroutineHolder
import po.misc.coroutines.CoroutineInfo
import po.misc.data.processors.FlowEmitter
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.context.subIdentity
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
    Faulty
}

sealed class TaskBase<T: CTX, R: Any?>(
    override val key: TaskKey,
    override val config: TaskConfig,
    dispatcher: TaskDispatcher,
    override val receiver: T,
): StaticHelper, MeasuredContext, ResultantTask<T, R>, LogInstance<T> {

    abstract var taskResult: TaskResult<R>?
    abstract val coroutineContext: CoroutineContext
    abstract val registry: TaskRegistry<*, *>
    abstract val callbackRegistry: CallbackManager<TaskDispatcher.UpdateType>
    abstract override val dataProcessor: LoggerDataProcessor
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(key.taskName, key.taskId.toString())
    abstract override val handler: TaskHandler<R>
    internal val actionSpans: MutableList<ActionSpan<*, *>> = mutableListOf()
    abstract val header: String
    val footer: String get() = "Footer"

    abstract fun start(): TaskBase<T, R>
    abstract fun complete(): TaskBase<T, R>
    abstract fun complete(managed: ManagedException): TaskBase<T, R>

    protected var taskStatus: ExecutionStatus = ExecutionStatus.Active
    override val executionStatus: ExecutionStatus get() = taskStatus



    internal fun lookUpRoot(): RootTask<*, *> {
        return registry.hierarchyRoot
    }



    override fun toString(): String {
       return dataProcessor.activeGroup?.groupHost?.formattedString?:"-----"
    }

    fun notifyUpdate(handler: TaskDispatcher.UpdateType) {
        val stats = LoggerStats(
            activeTask = this,
            activeTaskName = this.key.taskName,
            activeTaskNestingLevel = this.key.nestingLevel,
            topTasksCount = 1,
            totalTasksCount = registry.tasks.count(),
            coroutineInfo = CoroutineInfo.createInfo(coroutineContext)
        )
        callbackRegistry.trigger<LoggerStats>(handler, stats)
    }

    override fun changeStatus(status:ExecutionStatus){
        taskStatus = status
    }

    fun checkChildResult(childResult: TaskResult<*>): ManagedException? {
        return childResult.throwable?.let { exception ->
            val childTask = childResult.task
            childTask.taskStatus = ExecutionStatus.Faulty
            exception
        }
    }

    fun addActionSpan(actionSpan: ActionSpan<*, *>): ActionSpan<*, *> {
        actionSpans.add(actionSpan)
        return actionSpan
    }

    fun activeActionSpan(): ActionSpan<*, *>? {
        return actionSpans.firstOrNull { it.executionStatus == ExecutionStatus.Active }
    }

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

    fun createTaskData(builder: TaskBase<* , *>.()-> List<ActionData>): ErrorSnapshot{
        val result = this.builder()

        val taskData = ErrorSnapshot.errorSnapshotBuilder.build(this)

        taskData.actionSpanRecords = result
        return taskData
    }

//    fun createTaskData(actionRecords: List<ActionData>? = null): TaskData{
//        return actionRecords?.let {
//             TaskData(this).apply {
//                actionSpanRecords = actionRecords
//            }
//        }?:run {
//            TaskData(this)
//        }
//    }
}

class RootTask<T: CTX, R: Any?>(
    key : TaskKey,
    config: TaskConfig,
    override val coroutineContext: CoroutineContext,
    val dispatcher: TaskDispatcher,
    receiver: T,
):TaskBase<T, R>(key, config, dispatcher, receiver), CoroutineHolder{

    override val identity = subIdentity(this, receiver)

    override val dataProcessor: LoggerDataProcessor = LoggerDataProcessor(this, null, FlowEmitter())
    override var taskResult : TaskResult<R>?  =  null
    override val registry: TaskRegistry<T, R> = TaskRegistry(dispatcher, this)
    override val callbackRegistry = callbackManager<TaskDispatcher.UpdateType>()
    override val handler: TaskHandler<R> = TaskHandler(this, dataProcessor)
    override val header: String get()= "(R)[${key.taskName}] | Module[${key.moduleName}]".colorize(Colour.BLUE)

    val subTasksCount : Int get() = registry.taskCount()
    var isComplete: Boolean = false
    override val coroutineInfo : CoroutineInfo = CoroutineInfo.createInfo(coroutineContext)

    var escalationCallback : ((ManagedException)-> Unit)? = null

    internal fun commitSuicide(exception: ManagedException?){
       val cancellation =  exception?.let {
            CancellationException(it.message,  exception)
        }
        val job = coroutineContext[Job]
        if(job != null){
            dataProcessor.debug("Cancelling Job $job", "RootTask(commitSuicide)")
            job.cancel(cancellation)
        }else{
            dataProcessor.debug("Cancelling Context ${coroutineContext[CoroutineName]?.name?:"Unknown"}", "RootTask(commitSuicide)")
            coroutineContext.cancel(cancellation)
        }
    }

    override fun start(): TaskBase<T, R> {
        startTimer()
        dataProcessor.registerStart()
        return this
    }
    fun start(onEscalation: ((ManagedException)-> Unit)? = null):RootTask<T, R>{
        escalationCallback = onEscalation
        startTimer()
        dataProcessor.registerStart()
        return this
    }

    fun onChildCreated(childTask: Task<*, *>){
        notifyUpdate(TaskDispatcher.UpdateType.OnTaskUpdated)
        dispatcher.notifyUpdate(TaskDispatcher.UpdateType.OnTaskUpdated, this)
    }

    override fun complete():RootTask<T, R>{
        stopTimer()
        isComplete = true
        dataProcessor.registerStop()
        dispatcher.removeRootTask(this)
        return this
    }

    override fun complete(managed: ManagedException): Nothing{
        stopTimer()
        registry.setChildTasksStatus(ExecutionStatus.Faulty, this)
        changeStatus(ExecutionStatus.Failing)
        taskResult =  createFaultyResult(managed, this)
        isComplete = true
        dataProcessor.registerStop()
        dispatcher.removeRootTask(this)
        throw managed
    }

}

class Task<T: CTX,  R: Any?>(
    key : TaskKey,
    config: TaskConfig,
    internal val parentTask: TaskBase<*,*>,
    internal val hierarchyRoot: RootTask<*,*>,
    receiver: T
):TaskBase<T, R>(key, config, hierarchyRoot.dispatcher, receiver), ResultantTask<T, R>{


    override val identity = subIdentity(this, receiver)

    override val dataProcessor: LoggerDataProcessor = LoggerDataProcessor(this, hierarchyRoot.dataProcessor, null)
    override val coroutineContext: CoroutineContext get() = hierarchyRoot.coroutineContext
    override var taskResult : TaskResult<R>?  =  null
    override val registry: TaskRegistry<*, *> get() = hierarchyRoot.registry
    override val callbackRegistry =  callbackManager<TaskDispatcher.UpdateType>()
    override val handler: TaskHandler<R> = TaskHandler<R>(this, dataProcessor)
    override val coroutineInfo : CoroutineInfo = CoroutineInfo.createInfo(coroutineContext)
    override val header: String get() = "(${key.nestingLevel})[${key.taskName}] | Module[${key.moduleName}]".colorize(Colour.BLUE)
    fun notifyRootCancellation(exception: ManagedException?) {
        hierarchyRoot.commitSuicide(exception)
    }

    override fun start():Task<T, R>{
        startTimer()
        dataProcessor.registerStart()
        return this
    }
    override fun complete():Task<T, R>{
        stopTimer()
        dataProcessor.registerStop()
        return this
    }

    override fun complete(managed: ManagedException): TaskBase<T, R> {
        stopTimer()
        registry.setChildTasksStatus(ExecutionStatus.Faulty, this)
        changeStatus(ExecutionStatus.Failing)
        taskResult =  createFaultyResult(managed, this)
        dataProcessor.registerStop()
        throw managed
    }
}
