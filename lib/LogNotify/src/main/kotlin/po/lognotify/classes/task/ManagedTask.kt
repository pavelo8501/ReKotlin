package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import po.lognotify.classes.TaskResult
import po.lognotify.classes.notification.Notifier
import po.lognotify.exceptions.CancellationException
import po.lognotify.exceptions.DefaultException
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.PropagatedException
import po.lognotify.exceptions.SelfThrownException
import po.lognotify.exceptions.Terminator
import po.lognotify.exceptions.enums.CancelType
import po.lognotify.exceptions.enums.DefaultType
import po.lognotify.helpers.StaticsHelper
import po.lognotify.helpers.StaticsHelperProvider
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import kotlin.coroutines.CoroutineContext


class RootTask<R>(
    override val key : TaskKey,
    context: CoroutineContext
):TaskSealedBase<R>(key.taskName, context)
{

    override val nestingLevel: Int = key.nestingLevel

    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
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
):TaskSealedBase<R>(key.taskName, context), ControlledTask, ResultantTask
{

    override val taskName: String = key.taskName
    override val nestingLevel: Int = key.nestingLevel

    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
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
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>

    fun rootTaskOrNull(): RootTask<R>?{
        return this as? RootTask
    }

    internal suspend fun initializeComponents(){
        notifier.start()
    }

    protected fun stopTimer(): Float {
        val now = System.nanoTime()
        elapsed = (now - startTime) / 1_000_000f
        return elapsed
    }

    internal fun <R> createChildTask(name: String, hierarchyRoot:RootTask<*>): ManagedTask<R>{
        val lastRegistered = hierarchyRoot.registry.getLastRegistered()
        var childLevel =  lastRegistered.key.nestingLevel + 1
        val newChildTask = ManagedTask<R>(TaskKey(name, childLevel), hierarchyRoot.context, lastRegistered, hierarchyRoot)
        registry.registerChild(newChildTask)
        return newChildTask
    }

    suspend fun escalate(ex: Throwable): Throwable{
        notifier.systemInfo("Unhandled exception. Escalating", ex)
       val unmanaged = Terminator("Unhandled exception. Escalating")
       unmanaged.setSourceException(ex)
       throw unmanaged
    }

    private suspend fun handleException(throwable: Throwable): Throwable? {
        if(throwable is ExceptionBase) {
            val managedException = throwable
            when (managedException) {
                is PropagatedException -> {
                    if (!taskHelper.exceptionHandler.handlePropagated(throwable)) {
                        return throwable
                    }else{
                        return null
                    }
                }
                is CancellationException -> {
                    if (throwable.handlerType == CancelType.SKIP_SELF) {
                        notifier.systemInfo("Handled SKIP_SELF exception", managedException)
                        return throwable
                    }
                    if (throwable.handlerType == CancelType.CANCEL_ALL) {
                        if (taskHelper.exceptionHandler.handleCancellation(managedException)) {
                            return null
                        }
                        return throwable
                    }
                }

                is DefaultException -> {
                    if (throwable.handlerType == DefaultType.UNMANAGED) {
                        rootTaskOrNull()?.let {
                            managedException.rethrowSource()
                        }
                    }
                    if (throwable.handlerType == DefaultType.GENERIC) {
                        if (taskHelper.exceptionHandler.handleGeneric(managedException)) {
                           return null
                        }
                    }
                }
                else -> {
                    if (taskHelper.exceptionHandler.handleGeneric(throwable)) {
                       return null
                    }
                }
            }
        }
        return throwable
    }

    private suspend fun execute(block: suspend (TaskHandler) -> R): TaskResult<R> {
        val resultContainer = TaskResult<R>(this)
        try{
            val result = block.invoke(taskHelper)
            resultContainer.provideResult(stopTimer(), result)
        }catch (throwable: Throwable){
            val handledThrowable =   handleException(throwable)
            resultContainer.provideThrowable(stopTimer(), handledThrowable)
            return resultContainer
        }
        taskResult = resultContainer
        return resultContainer
    }
    private suspend fun <T> execute(receiver:T,  block: suspend T.(TaskHandler) -> R): TaskResult<R> {
        val resultContainer = TaskResult<R>(this)
        try {
           val result = block.invoke(receiver, taskHelper)
           resultContainer.provideResult(stopTimer(), result)
        }catch (throwable: Throwable){
           val handledThrowable =   handleException(throwable)
           resultContainer.provideThrowable(stopTimer(), handledThrowable)
           return resultContainer
        }
        taskResult = resultContainer
        return resultContainer
    }

    internal suspend fun runTask(block: suspend (TaskHandler) -> R): TaskResult<R> {
          return withContext(context) {
                 execute(block)
          }
    }
    internal suspend fun <T> runTask(receiver:T,  block: suspend T.(TaskHandler) -> R): TaskResult<R> {
        return  withContext(context) {
            execute(receiver, block)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun runTaskInDefaultContext(block: suspend (TaskHandler) -> R): TaskResult<R> {
      val result =  CoroutineScope(context).async {
          execute(block)
        }.getCompleted()
        return result
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun <T> runTaskInDefaultContext(receiver : T, block: suspend T.(TaskHandler) -> R): TaskResult<R> {
        val result =  CoroutineScope(context).async {
            execute(receiver, block)
        }.getCompleted()
        return result
    }

}