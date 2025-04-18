package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import po.lognotify.classes.taskresult.TaskResult
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.models.CoroutineInfo
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.LoggerException
import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import kotlin.coroutines.CoroutineContext


class RootTask<R>(
    val taskKey : TaskKey,
    context: CoroutineContext
):TaskSealedBase<R>(taskKey, context)
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult(this)
    override val registry: TaskRegistry<R> = TaskRegistry(this)
    override val taskHandler: TaskHandler<R> = TaskHandler(this)

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
    val taskKey : TaskKey,
    context: CoroutineContext,
    override val parent: TaskSealedBase<*>,
    private val hierarchyRoot : RootTask<*>,
):TaskSealedBase<R>(taskKey, context), ControlledTask, ResultantTask
{
    override val notifier: Notifier =  Notifier(this)
    override var taskResult : TaskResult<R> = TaskResult<R>(this)
    override val registry: TaskRegistry<*> = hierarchyRoot.registry
    override val taskHandler: TaskHandler<R> = TaskHandler<R>(this)

    override fun propagateToParent(th: Throwable) {
        throw th
    }

    suspend fun onUnhandledException(th: Throwable){
        taskResult.provideThrowable(stopTimer(), th)
    }

}

sealed class TaskSealedBase<R>(
    val key: TaskKey,
    val context: CoroutineContext,
 ): ResultantTask, StaticHelper
{

    override var startTime: Long = System.nanoTime()
    override var endTime: Long = 0L
    private var elapsed: Float = 0.0F


    override val nestingLevel: Int = key.nestingLevel
    override val qualifiedName: String = key.asString()
    override val taskName: String = key.taskName
    override val moduleName: String = key.moduleName?:"N/A"

    abstract val taskHandler: TaskHandler<R>
    abstract override val notifier : Notifier
    abstract var taskResult: TaskResult<R>
    abstract val registry: TaskRegistry<*>
    var isComplete: Boolean = false

    override var coroutineInfo: MutableList<CoroutineInfo> = mutableListOf<CoroutineInfo>()

    fun setCoroutineInfo(info : CoroutineInfo){
        coroutineInfo.add(info)
    }

    fun rootTaskOrNull(): RootTask<R>?{
        return this as? RootTask
    }

    internal suspend fun preRunConfig(scope: CoroutineScope){
       val info = CoroutineInfo(
            scope.coroutineContext.hashCode(),
            scope.coroutineContext[CoroutineName].toString()
        )
        this.setCoroutineInfo(info)
        notifier.start()
    }

    protected fun stopTimer(): Float {
        endTime = System.nanoTime()
        elapsed = (endTime - startTime) / 1_000_000f
        return elapsed
    }

    internal fun <R> createChildTask(name: String, hierarchyRoot:RootTask<*>, moduleName: String?): ManagedTask<R>{
        val lastRegistered = hierarchyRoot.registry.getLastRegistered()
        val childLevel =  lastRegistered.key.nestingLevel + 1
        val newChildTask = ManagedTask<R>(TaskKey(name, childLevel, moduleName), hierarchyRoot.context, lastRegistered, hierarchyRoot)
        registry.registerChild(newChildTask)
        return newChildTask
    }


    suspend fun handleException(throwable: Throwable): Throwable? {
        if(throwable is ManagedException) {
            val managedException = throwable
            when (throwable.handler) {
                HandlerType.CANCEL_ALL->{
                    if (throwable.handler == HandlerType.CANCEL_ALL) {
                        if (taskHandler.exceptionHandler.handleCancellation(managedException)) {
                            return null
                        }
                        return throwable
                    }
                }
                HandlerType.SKIP_SELF -> {
                    notifier.systemInfo(EventType.EXCEPTION_HANDLED, SeverityLevel.INFO, "Handled SKIP_SELF exception")
                    return throwable
                }

                HandlerType.GENERIC->{
                    if (taskHandler.exceptionHandler.handleGeneric(managedException)) {
                        return null
                    }
                    return managedException
                }

                HandlerType.UNMANAGED -> {
                    return throwable
                }
            }
        }else{
            if (taskHandler.exceptionHandler.handleGeneric(throwable)) {
                return null
            }
            return throwable
        }
        return throwable
    }

    private suspend fun execute(block: suspend (TaskHandler<R>) -> R): TaskResult<R> {
        val resultContainer = TaskResult<R>(this)
        try{
            val result = block.invoke(taskHandler)
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
           val result = block.invoke(receiver, taskHandler)
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
              async(start = CoroutineStart.UNDISPATCHED, context = context) {
                  execute(block)
              }.await()
          }
    }

    internal suspend fun <T> runTask(receiver:T,  block: suspend T.(TaskHandler<R>) -> R): TaskResult<R> {
        return withContext(context) {
            preRunConfig(this)
            async(start = CoroutineStart.UNDISPATCHED) {
                execute(receiver, block)
            }.await()
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
        val result = runBlocking {
            CoroutineScope(context).async{
                preRunConfig(this)
                execute(receiver, block)
            }.await()
        }
        return result
    }

}