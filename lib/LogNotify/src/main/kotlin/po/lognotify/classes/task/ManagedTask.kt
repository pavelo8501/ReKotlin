package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import po.lognotify.classes.jobs.ManagedJob
import po.lognotify.classes.taskresult.TaskResult
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.task.runner.TaskRunner
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.exceptions.CoroutineInfo
import po.misc.exceptions.getCoroutineInfo
import kotlin.coroutines.CoroutineContext


class RootTask<R: Any?>(
    val taskKey : TaskKey,
    coroutineContext: CoroutineContext
):TaskSealedBase<R>(taskKey, coroutineContext), ControlledTask
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult(this)
    override val registry: TaskRegistry<R> = TaskRegistry(this)
    override val taskHandler: TaskHandler<R> = TaskHandler(this as ControlledTask)

    override val taskRunner: TaskRunner<R> = TaskRunner(this as ControlledTask, taskHandler)

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

}

class ManagedTask<R: Any?>(
    val taskKey : TaskKey,
    coroutineContext: CoroutineContext,
    val parent: TaskSealedBase<*>,
    private val hierarchyRoot : RootTask<*>,
):TaskSealedBase<R>(taskKey, coroutineContext), ControlledTask, ResultantTask
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<*> = hierarchyRoot.registry
    override val taskHandler: TaskHandler<R> = TaskHandler<R>(this)
    override val taskRunner: TaskRunner<R> = TaskRunner(this as ControlledTask, taskHandler)

}

sealed class TaskSealedBase<R: Any?>(
    val key: TaskKey,
    val coroutineContext: CoroutineContext,
 ): ResultantTask, StaticHelper
{

    abstract val taskHandler: TaskHandler<R>
    abstract val notifier : Notifier
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>
    abstract val taskRunner: TaskRunner<R>

    var isComplete: Boolean = false

    override val startTime: Long
        get() { return taskRunner?.startTime?:0 }
    override var endTime : Long = 0
        get() { return taskRunner?.endTime?:0 }

    override val nestingLevel: Int = key.nestingLevel
    override val qualifiedName: String = key.asString()
    override val taskName: String = key.taskName
    override val moduleName: String = key.moduleName?:"N/A"

    override var coroutineInfo: MutableList<CoroutineInfo> = mutableListOf<CoroutineInfo>()

    val jobList = mutableListOf<ManagedJob>()

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

    suspend fun <T> runTaskInlined(receiver:T, block: suspend T.() -> R): TaskResult<R> {
        taskResult = TaskResult<R>(this)
        return withContext(coroutineContext){
            async(start = CoroutineStart.DEFAULT) {
                preRunConfig(this)
                taskRunner.execute(receiver, block) {
                    onResult { value, time ->
                        taskResult.provideResult(time, value)
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