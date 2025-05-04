package po.lognotify.classes.task

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import po.lognotify.classes.jobs.ManagedJob
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.sealed.ProviderTask
import po.lognotify.classes.task.runner.TaskRunner
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


class RootTask<R: Any?>(
    taskKey : TaskKey,
    coroutineContext: CoroutineContext,
    val dispatcher: TaskDispatcher,
) :TaskSealedBase<R>(taskKey, coroutineContext), ResultantTask, ControlledTask
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult(this)
    override val registry: TaskRegistry<R> = TaskRegistry(this)
    override val taskHandler: TaskHandler<R> = TaskHandler(this, exceptionHandler)
    override val taskRunner: TaskRunner<R> = TaskRunner(this, taskHandler, exceptionHandler)

    fun <R> createNewMemberTask(name : String, moduleName: String?): ManagedTask<R>{
        val lasEntry =  registry.getLastRegistered()
        when(lasEntry){
            is RootTask<*> -> {
                return lasEntry.createChildTask<R>(name, lasEntry, moduleName)
            }
            is ManagedTask ->{
                val asManagedTask = lasEntry
               return  asManagedTask.createChildTask<R>(name, this, moduleName)
            }
        }
    }
    override suspend fun notifyRootCancellation(exception: ManagedException?){
       val cancellation =  exception?.let {
            CancellationException(it.message,  exception.getSourceException(true))
        }
        val job = coroutineContext[Job]
        if(job != null){
            notifier.createTaskNotification(ProviderTask(this), "Cancelling Job ${job.toString()}", EventType.TASK_CANCELLATION,
                SeverityLevel.WARNING)
            job.cancel(cancellation)
        }else{
            notifier.createTaskNotification(ProviderTask(this), "Cancelling Context ${coroutineContext[CoroutineName]?.name?:"Unknown"}", EventType.TASK_CANCELLATION,
                SeverityLevel.WARNING)
            coroutineContext.cancel(cancellation)
        }
    }
    fun subTasksCount(): Int{
        return  registry.childTasks.count()
    }
}

class ManagedTask<R: Any?>(
    taskKey : TaskKey,
    coroutineContext: CoroutineContext,
    val parent: TaskSealedBase<*>,
    val hierarchyRoot: RootTask<*>,
):TaskSealedBase<R>(taskKey, coroutineContext), ControlledTask, ResultantTask
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<*> = hierarchyRoot.registry
    override val taskHandler: TaskHandler<R> = TaskHandler<R>(this,  exceptionHandler)
    override val taskRunner: TaskRunner<R> = TaskRunner(this as ControlledTask, taskHandler, exceptionHandler)

    override suspend fun notifyRootCancellation(exception: ManagedException?){
        hierarchyRoot.notifyRootCancellation(exception)
    }
}

sealed class TaskSealedBase<R: Any?>(
    override val key: TaskKey,
    override val coroutineContext: CoroutineContext,
 ): ResultantTask, StaticHelper, MeasuredContext
{
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(key.taskName, key.taskId.toString())

    abstract val taskHandler: TaskHandler<R>
    abstract override val notifier : Notifier
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>
    abstract val taskRunner: TaskRunner<R>
    val exceptionHandler: ExceptionHandler<R> = ExceptionHandler<R>(this)

     protected var hasCompleted: Boolean = false
     val isComplete: Boolean  get() = hasCompleted
     var coroutineInfo: MutableList<CoroutineInfo> = mutableListOf<CoroutineInfo>()

    val jobList = mutableListOf<ManagedJob>()

    fun notifyComplete(){
        hasCompleted = true
        if(this is RootTask){
            dispatcher.removeRootTask(this)
        }
    }

    suspend fun onBeforeTaskStart(){
        coroutineContext.currentProcess()?.let {
            it.observeTask(this)
        }
    }

    suspend fun onTaskStart(scope: CoroutineScope){
        coroutineInfo.add(scope.getCoroutineInfo())
        notifier.start()
        coroutineContext.currentProcess()?.let {
            notifier.systemInfo(EventType.START, SeverityLevel.INFO, it)
        }?:run {
            notifier.start()
        }
    }

    suspend fun onTaskComplete(){
        coroutineContext.currentProcess()?.let {
            notifier.systemInfo(EventType.STOP, SeverityLevel.INFO, it)
            it.stopTaskObservation(this)
        }
    }

    internal fun <R> createChildTask(name: String, hierarchyRoot:RootTask<*>, moduleName: String?): ManagedTask<R>{
        val lastRegistered = hierarchyRoot.registry.getLastRegistered()
        val childLevel =  lastRegistered.key.nestingLevel + 1
        val newChildTask = ManagedTask<R>(TaskKey(name, childLevel, moduleName), coroutineContext, lastRegistered, hierarchyRoot)
        newChildTask.notifier.setNotifierConfig(hierarchyRoot.notifier.getNotifierConfig())
        registry.registerChild(newChildTask)
        return newChildTask
    }

    suspend fun <T> runTaskInlined(receiver:T, block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)
        onBeforeTaskStart()
        return withContext(coroutineContext){
            async(start = CoroutineStart.DEFAULT) {
                onTaskStart(this)
                taskRunner.execute(receiver, block) {
                    onResult { value, time ->
                        taskResult.provideResult(time, value)
                    }
                    onUnhandled {exception, time->
                        taskResult.provideThrowable(time, exception)
                    }
                }
                onTaskComplete()
                taskResult
            }
        }.await()
    }

    internal suspend fun <T> runTask(receiver:T,  block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)
        onBeforeTaskStart()
        return withContext(coroutineContext) {
            onTaskStart(this)
            async(start = CoroutineStart.UNDISPATCHED) {
                taskRunner.execute(receiver, block){
                    onResult {value, time->
                        taskResult.provideResult(time, value)
                    }
                    onUnhandled {exception, time->
                        taskResult.provideThrowable(time, exception)
                    }
                }
                onTaskComplete()
                taskResult
            }.await()
        }
    }

    internal fun <T> runTaskAsync(receiver : T, block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)
        val result = runBlocking {
            onBeforeTaskStart()
            CoroutineScope(coroutineContext).async{
                onTaskStart(this)
                taskRunner.execute(receiver, block){
                    onResult {value, time->
                        taskResult.provideResult(time, value)
                    }
                    onUnhandled {exception, time->
                        taskResult.provideThrowable(time, exception)
                    }
                }
                onTaskComplete()
                taskResult
            }.await()
        }
        return result
    }

}