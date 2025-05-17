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
import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.SubNotifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.interfaces.ChildTask
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.interfaces.TopTask
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



sealed class TaskAsyncBase<R: Any?>(
    override val key: TaskKey,
    val coroutineContext: CoroutineContext,
): StaticHelper, MeasuredContext, ResultantTask
{
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(key.taskName, key.taskId.toString())

    abstract override val taskHandler: TaskHandler<R>
    abstract val notifier : NotifierBase
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>
    abstract val taskRunner: TaskRunner<R>
    val exceptionHandler: ExceptionHandler<R> = ExceptionHandler<R>(this)

    protected var hasCompleted: Boolean = false
    val isComplete: Boolean  get() = hasCompleted
    var coroutineInfo: MutableList<CoroutineInfo> = mutableListOf<CoroutineInfo>()

    val jobList = mutableListOf<ManagedJob>()
    fun  onBeforeTaskStart(){

    }

    fun notifyComplete(){
        hasCompleted = true
        if(this is RootTask){
            dispatcher.removeRootTask(this)
        }
    }

    suspend fun onTaskStart(scope: CoroutineScope){
        coroutineInfo.add(scope.getCoroutineInfo())
        notifier.systemInfo(EventType.START, SeverityLevel.SYS_INFO)

        coroutineContext.currentProcess()?.let {
            notifier.systemInfo(EventType.START, SeverityLevel.INFO, it)
        }
    }

    abstract fun onTaskComplete()

    internal fun <R> createChildTask(name: String, hierarchyRoot:RootTask<*>, moduleName: String?): SubTask<R>{
        val lastRegistered = hierarchyRoot.registry.getLastRegistered()
        val childLevel =  lastRegistered.key.nestingLevel + 1
        val effectiveModuleName = moduleName?:hierarchyRoot.key.moduleName
        val newChildTask = SubTask<R>(TaskKey(name, childLevel, effectiveModuleName), coroutineContext, lastRegistered, hierarchyRoot)
        newChildTask.notifier.setNotifierConfig(hierarchyRoot.notifier.getNotifierConfig())
        registry.registerChild(newChildTask)
        return newChildTask
    }

    suspend fun <T> runTaskInlined(receiver:T, block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)

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


class RootTask<R: Any?>(
    taskKey : TaskKey,
    coroutineContext: CoroutineContext,
    val dispatcher: TaskDispatcher,
) :TaskAsyncBase<R>(taskKey, coroutineContext), TopTask<R>
{
    override val notifier: RootNotifier =  RootNotifier(this)
    override var taskResult : TaskResult<R> = TaskResult(this)
    override val registry: TaskRegistry<R> = TaskRegistry(this)
    override val taskHandler: TaskHandler<R> = TaskHandler(this, exceptionHandler)
    override val taskRunner: TaskRunner<R> = TaskRunner(this, taskHandler, exceptionHandler)


    override val subTasksCount : Int get() = registry.childTasks.count()

    fun <R> createNewMemberTask(name : String, moduleName: String?): SubTask<R>{
        val lasEntry =  registry.getLastRegistered()
        when(lasEntry){
            is RootTask<*> -> {
                return lasEntry.createChildTask<R>(name, lasEntry, moduleName)
            }
            is SubTask ->{
                val asManagedTask = lasEntry
               return  asManagedTask.createChildTask<R>(name, this, moduleName)
            }
        }
    }

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
    fun lastTask(): TaskAsyncBase<*>{
       return registry.getLastRegistered()
    }

    suspend fun emitTaskComplete() {
        coroutineContext.currentProcess()?.let {
            notifier.systemInfo(EventType.STOP, SeverityLevel.INFO, it)
            it.stopTaskObservation(this)
        }
    }

    override fun onTaskComplete() {
        notifier.systemInfo(EventType.STOP, SeverityLevel.SYS_INFO)
    }

}

class SubTask<R: Any?>(
    taskKey : TaskKey,
    coroutineContext: CoroutineContext,
    val parent: TaskAsyncBase<*>,
    val hierarchyRoot: RootTask<*>,
):TaskAsyncBase<R>(taskKey, coroutineContext), ResultantTask, ChildTask {
    override val notifier: SubNotifier = SubNotifier(this, hierarchyRoot.notifier)
    override var taskResult: TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<*> = hierarchyRoot.registry
    override val taskHandler: TaskHandler<R> = TaskHandler<R>(this, exceptionHandler)
    override val taskRunner: TaskRunner<R> = TaskRunner(this, taskHandler, exceptionHandler)

    fun notifyRootCancellation(exception: ManagedException?) {
        hierarchyRoot.commitCancellation(exception)
    }

    override fun onTaskComplete() {
        notifier.systemInfo(EventType.STOP, SeverityLevel.SYS_INFO)
    }
}
