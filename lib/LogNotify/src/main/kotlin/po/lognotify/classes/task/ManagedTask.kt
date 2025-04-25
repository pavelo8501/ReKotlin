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
import po.lognotify.classes.task.TaskResult
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.models.TaskData
import po.lognotify.classes.task.runner.TaskRunner
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.exceptions.CoroutineInfo
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.getCoroutineInfo
import kotlin.coroutines.CoroutineContext


class RootTask<R: Any?>(
    taskKey : TaskKey,
    coroutineContext: CoroutineContext,
    val dispatcher: TaskDispatcher,
    override val taskData : TaskData = TaskData(taskKey,"", 0, 0, coroutineContext),
) :TaskSealedBase<R>(taskKey, coroutineContext, taskData), ResultantTask, ControlledTask
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult(this)
    override val registry: TaskRegistry<R> = TaskRegistry(this)
    override val taskHandler: TaskHandler<R> = TaskHandler(this, taskData, exceptionHandler)
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
            notifier.createTaskNotification(this, "Cancelling Job ${job.toString()}", EventType.TASK_CANCELLATION,
                SeverityLevel.WARNING)
            job.cancel(cancellation)
        }else{
            notifier.createTaskNotification(this, "Cancelling Context ${coroutineContext[CoroutineName]?.name?:"Unknown"}", EventType.TASK_CANCELLATION,
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
    override val taskData : TaskData = TaskData(taskKey,"", 0, 0, coroutineContext)
):TaskSealedBase<R>(taskKey, coroutineContext, taskData), ControlledTask, ResultantTask
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<*> = hierarchyRoot.registry
    override val taskHandler: TaskHandler<R> = TaskHandler<R>(this, taskData, exceptionHandler)
    override val taskRunner: TaskRunner<R> = TaskRunner(this as ControlledTask, taskHandler, exceptionHandler)

    override suspend fun notifyRootCancellation(exception: ManagedException?){
        hierarchyRoot.notifyRootCancellation(exception)
    }
}

sealed class TaskSealedBase<R: Any?>(
    val key: TaskKey,
    val coroutineContext: CoroutineContext,
    override val taskData: TaskData,
 ): ResultantTask, StaticHelper
{

    abstract val taskHandler: TaskHandler<R>
    abstract val notifier : Notifier
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>
    abstract val taskRunner: TaskRunner<R>
    val exceptionHandler = ExceptionHandler<R>(this)

     protected var hasCompleted: Boolean = false
     val isComplete: Boolean  get() = hasCompleted
     var coroutineInfo: MutableList<CoroutineInfo> = mutableListOf<CoroutineInfo>()

    val jobList = mutableListOf<ManagedJob>()

    fun notifyComplete(){
        hasCompleted = true
        if(this is RootTask ){
            dispatcher.removeRootTask(this)
        }
    }

    fun setCoroutineInfo(info : CoroutineInfo){
        coroutineInfo.add(info)
    }


    suspend fun preRunConfig(scope: CoroutineScope){
        this.setCoroutineInfo(scope.getCoroutineInfo())
        notifier.start()
    }
    internal fun <R> createChildTask(name: String, hierarchyRoot:RootTask<*>, moduleName: String?): ManagedTask<R>{
        val lastRegistered = hierarchyRoot.registry.getLastRegistered()
        val childLevel =  lastRegistered.key.nestingLevel + 1
        val newChildTask = ManagedTask<R>(TaskKey(name, childLevel, moduleName), coroutineContext, lastRegistered, hierarchyRoot)
        registry.registerChild(newChildTask)
        return newChildTask
    }


    suspend fun <T> runTaskInlined(receiver:T, block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)
        return withContext(coroutineContext){
            async(start = CoroutineStart.DEFAULT) {
                preRunConfig(this)
                taskRunner.execute(receiver, block) {
                    onResult { value, time ->
                        taskResult.provideResult(time, value)
                    }
                    onUnhandled {exception, time->
                        taskResult.provideThrowable(time, exception)
                    }
                }
                taskResult
            }
        }.await()
    }

    internal suspend fun <T> runTask(receiver:T,  block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)
        return withContext(coroutineContext) {
            preRunConfig(this)
            async(start = CoroutineStart.UNDISPATCHED) {
                taskRunner.execute(receiver, block){
                    onResult {value, time->
                        taskResult.provideResult(time, value)
                    }
                    onUnhandled {exception, time->
                        taskResult.provideThrowable(time, exception)
                    }
                }
                taskResult
            }.await()
        }
    }

    internal fun <T> runTaskAsync(receiver : T, block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)
        val result = runBlocking {
            CoroutineScope(coroutineContext).async{
                preRunConfig(this)
                taskRunner.execute(receiver, block){
                    onResult {value, time->
                        taskResult.provideResult(time, value)
                    }
                    onUnhandled {exception, time->
                        taskResult.provideThrowable(time, exception)
                    }
                }
                taskResult
            }.await()
        }
        return result
    }

}