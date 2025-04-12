package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import po.lognotify.classes.taskresult.TaskResult
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.CancellationException
import po.lognotify.exceptions.DefaultException
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.LoggerException
import po.lognotify.exceptions.SelfThrownException
import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import kotlin.coroutines.CoroutineContext


class RootTask<R>(
    override val key : TaskKey,
    context: CoroutineContext
):TaskSealedBase<R>(key.taskName, context)
{

    override val qualifiedName: String = key.asString()
    override val taskName: String = key.taskName
    override val nestingLevel: Int = key.nestingLevel

    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<R> = TaskRegistry<R>(this)
    override val taskHelper: TaskHandler<R> = TaskHandler<R>(this)

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

    override val qualifiedName: String = key.asString()
    override val taskName: String = key.taskName
    override val nestingLevel: Int = key.nestingLevel

    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<*> = hierarchyRoot.registry
    override val taskHelper: TaskHandler<R> = TaskHandler<R>(this)

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
 ) : ResultantTask, SelfThrownException, StaticHelper
{


    override var startTime: Long = System.nanoTime()
    override var endTime: Long = 0L
    private var elapsed: Float = 0.0F

    abstract  val key : TaskKey

    abstract val taskHelper: TaskHandler<R>
    abstract override val notifier : Notifier
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>

    fun rootTaskOrNull(): RootTask<R>?{
        return this as? RootTask
    }

    internal suspend fun preRunConfig(scope: CoroutineScope){
        notifier.start()
    }

    protected fun stopTimer(): Float {
        endTime = System.nanoTime()
        elapsed = (endTime - startTime) / 1_000_000f
        return elapsed
    }

    internal fun <R> createChildTask(name: String, hierarchyRoot:RootTask<*>, moduleName: String?): ManagedTask<R>{
        val lastRegistered = hierarchyRoot.registry.getLastRegistered()
        var childLevel =  lastRegistered.key.nestingLevel + 1
        val newChildTask = ManagedTask<R>(TaskKey(name, childLevel, moduleName), hierarchyRoot.context, lastRegistered, hierarchyRoot)
        registry.registerChild(newChildTask)
        return newChildTask
    }

    suspend fun escalate(ex: Throwable): Throwable{
        notifier.systemInfo("Unhandled exception. Escalating", EventType.START, SeverityLevel.EXCEPTION)
       val unmanaged = LoggerException("Unhandled exception. Escalating")
       unmanaged.setSourceException(ex)
       throw unmanaged
    }

    private suspend fun handleException(throwable: Throwable): Throwable? {
        if(throwable is ExceptionBase) {
            val managedException = throwable
            when (managedException) {
                is CancellationException -> {
                    if (managedException.handler == HandlerType.SKIP_SELF) {
                        notifier.systemInfo("Handled SKIP_SELF exception", EventType.EXCEPTION_HANDLED, SeverityLevel.INFO )
                        return throwable
                    }
                    if (throwable.handler == HandlerType.CANCEL_ALL) {
                        if (taskHelper.exceptionHandler.handleCancellation(managedException)) {
                            return null
                        }
                        return throwable
                    }
                }
                is DefaultException -> {
                    if (taskHelper.exceptionHandler.handleGeneric(managedException)) {
                        return null
                    }
                    return managedException
                }
                is LoggerException -> {
                    return managedException
                }
            }
        }else{
            if (taskHelper.exceptionHandler.handleGeneric(throwable)) {
                return null
            }
            return throwable
        }
        return throwable
    }

    private suspend fun execute(block: suspend (TaskHandler<R>) -> R): TaskResult<R> {
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
    private suspend fun <T> execute(receiver:T,  block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
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

    internal suspend fun runTask(block: suspend (TaskHandler<R>) -> R): TaskResult<R> {
          return withContext(context) {
              preRunConfig(this)
              execute(block)
          }
    }
    internal suspend fun <T> runTask(receiver:T,  block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        return  withContext(context) {
            preRunConfig(this)
            execute(receiver, block)
        }
    }

    internal fun runTaskAsync(block: suspend (TaskHandler<R>) -> R): TaskResult<R> {
        val result = runBlocking {
            CoroutineScope(context).async {
                preRunConfig(this)
                execute(block)
            }.await()
        }
        return result
    }
    internal fun <T> runTaskAsync(receiver : T, block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
       val result =  runBlocking {
             CoroutineScope(context).async {
                 preRunConfig(this)
                 execute(receiver, block)
            }.await()
        }
        return result
    }

}