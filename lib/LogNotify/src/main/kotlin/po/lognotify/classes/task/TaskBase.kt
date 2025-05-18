package po.lognotify.classes.task

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import po.lognotify.classes.jobs.ManagedJob
import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.SubNotifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.interfaces.TopTask
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.extensions.currentProcess
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.exceptions.CoroutineInfo
import po.misc.exceptions.ManagedException
import po.misc.exceptions.getCoroutineInfo
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import kotlin.coroutines.CoroutineContext


sealed class TaskBase<R: Any?>(
    override val key: TaskKey,
    dispatcher: TaskDispatcher,
): StaticHelper, MeasuredContext, ResultantTask<R> {

    abstract val coroutineContext: CoroutineContext
    abstract override val notifier : NotifierBase
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>

    override val exceptionHandler: ExceptionHandler<R> = ExceptionHandler<R>(this)
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(key.taskName, key.taskId.toString())

    abstract override val handler: TaskHandler<R>

    var coroutineInfo: MutableList<CoroutineInfo> = mutableListOf<CoroutineInfo>()

    val jobList = mutableListOf<ManagedJob>()
    fun  onBeforeTaskStart(){

    }

    suspend fun onTaskStart(scope: CoroutineScope){
        coroutineInfo.add(scope.getCoroutineInfo())
        notifier.systemInfo(EventType.START, SeverityLevel.SYS_INFO)
        coroutineContext.currentProcess()?.let {
            notifier.systemInfo(EventType.START, SeverityLevel.INFO, it)
        }
    }
    abstract fun onTaskComplete()
}


class RootTask<R: Any?>(
    key : TaskKey,
    override val coroutineContext: CoroutineContext,
    val dispatcher: TaskDispatcher,
) :TaskBase<R>(key, dispatcher)
{
    override val notifier: RootNotifier<R> =  RootNotifier(this)
    override var taskResult : TaskResult<R> = TaskResult(this)
    override val registry: TaskRegistry<R> = TaskRegistry(this)
    override val handler: TaskHandler<R> = TaskHandler(this, exceptionHandler)

    val subTasksCount : Int get() = registry.taskCount()
    val isComplete: Boolean = false

    fun commitCancellation(exception: ManagedException?){
       val cancellation =  exception?.let {
            CancellationException(it.message,  exception.getSourceException(true))
        }
        val job = coroutineContext[Job]
        if(job != null){
            notifier.systemInfo(EventType.TASK_CANCELLATION, SeverityLevel.EXCEPTION, "Cancelling Job $job")
            job.cancel(cancellation)
        }else{
            notifier.systemInfo(EventType.TASK_CANCELLATION, SeverityLevel.EXCEPTION, "Cancelling Context ${coroutineContext[CoroutineName]?.name?:"Unknown"}")
            coroutineContext.cancel(cancellation)
        }
    }
    suspend fun emitTaskComplete() {
        coroutineContext.currentProcess()?.let {
            notifier.systemInfo(EventType.STOP, SeverityLevel.INFO, it)
            it.stopTaskObservation(this)
        }
    }

    fun notifyComplete(){
        dispatcher.removeRootTask(this)
    }

    override fun onTaskComplete() {
        notifier.systemInfo(EventType.STOP, SeverityLevel.SYS_INFO)
    }
}

class Task<R: Any?>(
    key : TaskKey,
    val parentTask: TaskBase<*>,
    val hierarchyRoot: RootTask<*>,
):TaskBase<R>(key, hierarchyRoot.dispatcher), ResultantTask<R>{

    override val coroutineContext: CoroutineContext get() = hierarchyRoot.coroutineContext

    override val notifier: SubNotifier = SubNotifier(this, hierarchyRoot.notifier)
    override var taskResult: TaskResult<R> = TaskResult(this)
    override val registry: TaskRegistry<*> get() = hierarchyRoot.registry
    override val handler: TaskHandler<R> = TaskHandler<R>(this, exceptionHandler)

    fun notifyRootCancellation(exception: ManagedException?) {
        hierarchyRoot.commitCancellation(exception)
    }

    override fun onTaskComplete() {
        notifier.systemInfo(EventType.STOP, SeverityLevel.SYS_INFO)
    }
}

fun createTaskKey(name: String, moduleName: String, nestingLevel: Int = 0): TaskKey{
    return TaskKey(name, nestingLevel, moduleName)
}

fun <R> Task<*>.createChild(name: String, moduleName: String): Task<R>{
    val task =  Task<R>(createTaskKey(name, moduleName, registry.taskCount()+1), this, hierarchyRoot)
    registry.registerChild(task)
    return task
}

fun <R> RootTask<*>.createChild(name: String, moduleName: String): Task<R>{
    val task =  Task<R>(createTaskKey(name, moduleName, registry.taskCount()+1), this, this)
    registry.registerChild(task)
    return task
}