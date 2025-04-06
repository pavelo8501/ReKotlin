package po.managedtask.classes.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import po.managedtask.classes.ManagedResult
import po.managedtask.classes.notification.Notifier
import po.managedtask.classes.TaskResult
import po.managedtask.enums.SeverityLevel
import po.managedtask.exceptions.CancellationException
import po.managedtask.exceptions.DefaultException
import po.managedtask.exceptions.ExceptionBase
import po.managedtask.exceptions.PropagatedException
import po.managedtask.exceptions.SelfThrownException
import po.managedtask.exceptions.Terminator
import po.managedtask.exceptions.enums.CancelType
import po.managedtask.exceptions.enums.DefaultType
import po.managedtask.extensions.getOrThrowDefault
import po.managedtask.extensions.safeCast
import po.managedtask.helpers.StaticsHelper

import po.managedtask.helpers.StaticsHelperProvider
import po.managedtask.models.TaskKey
import po.managedtask.models.TaskRegistry
import kotlin.coroutines.CoroutineContext


class RootTask<R>(
    override val key : TaskKey,
    context: CoroutineContext
):TaskSealedBase<R>(key.taskName, context)
{

    override val notifier: Notifier =  Notifier(this)
    override val taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<R> = TaskRegistry<R>(this)
    override val taskHelper: TaskHandler = TaskHandler(this)

    fun <R> createNewMemberTask(name : String): ManagedTask<R>{
        val lasEntry =  registry.getLastRegistered()
        when(lasEntry){
            is RootTask<*> -> {
                return lasEntry.createChildTask<R>(name, lasEntry)
            }
            is ManagedTask ->{
                val asManagedTask = lasEntry
               return  asManagedTask.createChildTask<R>(name, this)

            }
        }
    }

    suspend fun onUnhandledException(th: Throwable){
        taskResult.provideThrowable(stopTimer(), th)
    }

}

class ManagedTask<R>(
    override val key : TaskKey,
    context: CoroutineContext,
    override val parent: TaskSealedBase<*>,
    private val hierarchyRoot : RootTask<*>,
):TaskSealedBase<R>(key.taskName, context), ControlledTask
{

    override val notifier: Notifier =  Notifier(this)
    override val taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<*> = hierarchyRoot.registry
    override val taskHelper: TaskHandler = TaskHandler(this)

    override fun propagateToParent(th: Throwable) {
        throw th
    }

    suspend fun onUnhandledException(th: Throwable){
        taskResult.provideThrowable(stopTimer(), th)
    }
}

sealed class TaskSealedBase<R>(
    override val taskName : String,
    val context: CoroutineContext,
    val helper: StaticsHelperProvider = StaticsHelper(taskName)
 ) : ResultantTask, SelfThrownException, StaticsHelperProvider by helper
{

    private var startTime: Long = System.nanoTime()
    private var elapsed: Float = 0.0F

    abstract  val key : TaskKey

    abstract val taskHelper: TaskHandler

    abstract val notifier : Notifier
    abstract val taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>

    fun rootTaskOrNull(): RootTask<R>?{
        return this as? RootTask
    }

    internal suspend fun initializeComponents(){
        taskHelper.notifier.start()

    }

    protected fun stopTimer(): Float {
        val now = System.nanoTime()
        elapsed = (now - startTime) / 1_000_000f
        return elapsed
    }
    private suspend fun executeTask(block: suspend TaskHandler.(StaticsHelperProvider) -> R): ManagedResult<R> {
        val result = block.invoke(taskHelper, helper)
        stopTimer()
        taskResult.provideResult(elapsed, result)
        return taskResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Result cast failed")
    }

    internal fun <R> createChildTask(name: String, hierarchyRoot:RootTask<*>): ManagedTask<R>{
        val lastRegistered = hierarchyRoot.registry.getLastRegistered()
        var childLevel =  lastRegistered.key.nestingLevel + 1
        val newChildTask = ManagedTask<R>(TaskKey(name, childLevel), hierarchyRoot.context, lastRegistered, hierarchyRoot)
        registry.registerChild(newChildTask)
        return newChildTask
    }

    internal suspend fun escalate(ex: Throwable){
        notifier.systemInfo("Unhandled exception. Escalating", ex)
        withContext(context) {
           val unmanaged =  Terminator("Unhandled exception. Escalating")
           unmanaged.setSourceException(ex)
           throw unmanaged
        }
    }
    protected suspend fun handleException(throwable: Throwable): ManagedResult<R> {
       val time =  stopTimer()
        if(throwable is ExceptionBase) {
            val managedException = throwable
            when (managedException) {
                is PropagatedException -> {
                    if (!taskHelper.handler.handlePropagated(throwable)) {
                        taskResult.provideThrowable(time, throwable)
                        rootTaskOrNull().let {
                            val msg = "Exception reached task execution top level but no handler was provided."
                            notifier.systemInfo("Unhandled PROPAGATED exception. $msg", managedException)
                        }
                    }
                }
                is CancellationException -> {
                    if (throwable.handlerType == CancelType.SKIP_SELF) {
                        notifier.systemInfo("Handled SKIP_SELF exception", managedException)
                    }
                    if (throwable.handlerType == CancelType.CANCEL_ALL) {
                        if (!taskHelper.handler.handleCancellation(managedException)) {
                            taskResult.provideThrowable(time, managedException)
                        }
                    }
                }

                is DefaultException -> {
                    if (throwable.handlerType == DefaultType.UNMANAGED) {
                        rootTaskOrNull()?.let {
                            managedException.rethrowSource()
                        }
                    }
                    if (throwable.handlerType == DefaultType.GENERIC) {
                        if (!taskHelper.handler.handleGeneric(managedException)) {
                            taskResult.provideThrowable(time, managedException)
                        }
                    }
                }
                else -> {
                    if (!taskHelper.handler.handleGeneric(throwable)) {
                        taskResult.provideThrowable(time, throwable)
                    }
                }
            }
        }
        return taskResult
    }

    suspend fun runTask(block: suspend TaskHandler.(StaticsHelperProvider) -> R): ManagedResult<R> {
        runCatching {
            return withContext(context) {
                notifier.systemInfo("Starting", SeverityLevel.INFO)
                val result = executeTask(block)
                notifier.systemInfo("Complete", SeverityLevel.INFO)
                result
            }
        }.getOrElse {
            notifier.systemInfo("Handling exception with message :${it.message}", SeverityLevel.INFO)
            return handleException(it)
        }
    }

    fun runTaskInDefaultContext(block: suspend TaskHandler.(StaticsHelperProvider) -> R): Deferred<ManagedResult<R>> =
        CoroutineScope(this.context).async {
            runCatching {
                notifier.systemInfo("Starting", SeverityLevel.INFO)
                val result = executeTask(block)
                notifier.systemInfo("Complete", SeverityLevel.INFO)
                result
            }.getOrElse {
                notifier.systemInfo("Task Runner Out of coroutine scope", it)
                handleException(it)
            }
        }

}