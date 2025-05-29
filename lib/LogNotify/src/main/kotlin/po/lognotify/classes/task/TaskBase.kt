package po.lognotify.classes.task

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.SubNotifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.extensions.currentProcess
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskDispatcher.LoggerStats
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.coroutines.CoroutineHolder
import po.misc.exceptions.CoroutineInfo
import po.misc.exceptions.ManagedException
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.startTimer
import po.misc.time.stopTimer
import po.misc.types.UpdateType
import kotlin.coroutines.CoroutineContext

sealed class TaskBase<R: Any?>(
    override val key: TaskKey,
    override val config: TaskConfig,
    dispatcher: TaskDispatcher,
): StaticHelper, MeasuredContext, ResultantTask<R> {

    abstract val coroutineContext: CoroutineContext
    abstract override val notifier : NotifierBase
    abstract val registry: TaskRegistry<*>
    abstract val callbackRegistry : MutableMap<UpdateType, (LoggerStats)-> Unit>

    override val exceptionHandler: ExceptionHandler<R> = ExceptionHandler<R>(this)
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(key.taskName, key.taskId.toString())
    abstract override val handler: TaskHandler<R>

    fun notifyUpdate(handler: UpdateType) {
        val stats = LoggerStats(
            activeTask = this,
            activeTaskName = this.key.taskName,
            activeTaskNestingLevel = this.key.nestingLevel,
            topTasksCount =  1,
            totalTasksCount = registry.tasks.count(),
            coroutineInfo = CoroutineInfo.createInfo(coroutineContext)
        )
        callbackRegistry.filter { it.key == handler} .forEach { (_, cb) -> cb(stats) }
    }

    abstract fun onStart():TaskBase<R>
}

class RootTask<R: Any?>(
    key : TaskKey,
    config: TaskConfig,
    override val coroutineContext: CoroutineContext,
    val dispatcher: TaskDispatcher,
) :TaskBase<R>(key, config, dispatcher), CoroutineHolder
{
    override val notifier: RootNotifier<R> =  RootNotifier(this)
    //override var taskResult : TaskResult<R>?  =  null
    override val registry: TaskRegistry<R> = TaskRegistry(dispatcher, this)
    override val callbackRegistry: MutableMap<UpdateType, (LoggerStats) -> Unit> = mutableMapOf()
    override val handler: TaskHandler<R> = TaskHandler(this, exceptionHandler)
    val subTasksCount : Int get() = registry.taskCount()
    var isComplete: Boolean = false
    override val coroutineInfo : CoroutineInfo = CoroutineInfo.createInfo(coroutineContext)

    internal fun commitSuicide(exception: ManagedException?){
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

    override fun onStart():RootTask<R>{
        startTimer()
        notifier.systemInfo(EventType.START, SeverityLevel.SYS_INFO)
        coroutineContext.currentProcess()?.let {
            notifier.systemInfo(EventType.START, SeverityLevel.INFO, it)
            it.stopTaskObservation(this)
        }
        return this
    }

    fun onChildCreated(childTask: Task<*>){
//        notifier.submitNotification(
//            Notification(ProviderTask(this), EventType.CHILD_TASK_CREATED, SeverityLevel.SYS_INFO,childTask.toString())
//        )
        notifyUpdate(UpdateType.OnUpdated)
        dispatcher.notifyUpdate(UpdateType.OnUpdated, this)
    }

    fun onComplete():RootTask<R>{
        stopTimer()
        isComplete = true
        notifier.systemInfo(EventType.STOP, SeverityLevel.SYS_INFO)
        dispatcher.removeRootTask(this)
        return this
    }
}

class Task<R: Any?>(
    key : TaskKey,
    config: TaskConfig,
    val parentTask: TaskBase<*>,
    val hierarchyRoot: RootTask<*>,
):TaskBase<R>(key, config, hierarchyRoot.dispatcher), ResultantTask<R>{

    override val coroutineContext: CoroutineContext get() = hierarchyRoot.coroutineContext
    override val notifier: SubNotifier = SubNotifier(this, hierarchyRoot.notifier)
    override val registry: TaskRegistry<*> get() = hierarchyRoot.registry
    override val callbackRegistry: MutableMap<UpdateType, (LoggerStats) -> Unit> = mutableMapOf()
    override val handler: TaskHandler<R> = TaskHandler<R>(this, exceptionHandler)
    override val coroutineInfo : CoroutineInfo = CoroutineInfo.createInfo(coroutineContext)

    fun notifyRootCancellation(exception: ManagedException?) {
        hierarchyRoot.commitSuicide(exception)
    }

    override fun onStart():Task<R>{
        startTimer()
        notifier.systemInfo(EventType.START, SeverityLevel.SYS_INFO)
        return this
    }
    fun onComplete():Task<R>{
        stopTimer()
        notifier.systemInfo(EventType.STOP, SeverityLevel.SYS_INFO)
        return this
    }
}
