package po.managedtask.classes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import po.managedtask.exceptions.DefaultException
import po.managedtask.exceptions.ExceptionHandler
import po.managedtask.exceptions.ExceptionThrower
import po.managedtask.exceptions.ExceptionsThrown
import po.managedtask.exceptions.ManagedExceptionBase
import po.managedtask.exceptions.ManagedHandleType
import po.managedtask.extensions.getOrThrow
import po.managedtask.extensions.safeCast

import po.managedtask.helpers.StaticsHelperProvider
import kotlin.coroutines.CoroutineContext

interface ResultantTask : ExceptionHandler {
    val parent: ResultantTask?
    val taskName: String
    val childTasks: MutableList<ResultantTask>
    val taskResult: TaskResult<*>
    val thrower: ExceptionsThrown

    fun handleException(th: Throwable)
    fun getContext():CoroutineContext
}

class ManagedTask<R : Any?>(
    private val helper: StaticsHelperProvider,
    override val thrower: ExceptionsThrown,
    override var parent: ResultantTask? = null,
    private val block: suspend ManagedTask<Any?>.(StaticsHelperProvider) -> R?,
) : ResultantTask, StaticsHelperProvider by helper, ExceptionsThrown by thrower, CoroutineScope {

    override val taskName: String =  helper.moduleName
    private var startTime: Long = System.nanoTime()
    private var elapsed: Float = 0.0F

    override val taskResult: TaskResult<R?> = TaskResult(taskName)
    override val childTasks: MutableList<ResultantTask> = mutableListOf()
    override var coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default
    val notifier  = Notifier(helper)

    private fun stopTimer() {
        val now = System.nanoTime()
        elapsed = (now - startTime) / 1_000_000f
    }

    internal fun withCoroutine(context: CoroutineContext){
        coroutineContext = context
    }

    private var propagatedHandler: ((ex: ManagedExceptionBase)-> Unit)? = null
    override fun setPropagatedExHandler(handlerFn: (ex: ManagedExceptionBase)-> Unit){
        propagatedHandler = handlerFn
    }
    private fun handlePropagatedException(ex: ManagedExceptionBase){
        propagatedHandler?.invoke(ex)?:throw ex
    }

    var canselHandler : ((ex: ManagedExceptionBase)-> Unit)? = null
    override fun setCancellationExHandler(handlerFn: (ex: ManagedExceptionBase)-> Unit){
        canselHandler = handlerFn
    }

    var genericHandler : ((ex: Throwable)-> Unit)? = null
    override fun setGenericExHandler(handlerFn: (ex: Throwable)-> Unit){
        genericHandler = handlerFn
    }
    private fun handleGenericException(th: Throwable){
        error(th)
        genericHandler?.invoke(th)?:throw th
    }

    override fun handleException(th: Throwable) {
        when (th) {
            is ManagedExceptionBase -> {
                when (th.handleType) {
                    ManagedHandleType.DEFAULT -> {
                        error(th)
                        handleGenericException(th)
                    }
                    ManagedHandleType.SKIP_SELF -> {
                        error(th)
                    }
                    ManagedHandleType.PROPAGATED -> {
                        error(th)
                        val thisParent = parent
                        if(thisParent != null){
                            thisParent.handleException(th)
                        }else{
                            handlePropagatedException(th)
                        }
                    }
                    ManagedHandleType.CANCEL_ALL -> {
                        error(th)
                        if(th.invokeCancellation() == false) {
                            if (canselHandler != null) {
                                canselHandler!!.invoke(th)
                            } else {
                                throw th
                            }
                        }
                    }
                }
            }
            else -> {
                handleGenericException(th)
            }
        }
    }

    override fun getContext(): CoroutineContext {
        return coroutineContext
    }

    suspend fun runTask(): TaskResult<R?> {

        parent?.also {thisParent->
            thisParent.childTasks.add(this)
            withCoroutine(thisParent.getContext())
        }
        val taskExecutionResult = executeTask()
        return taskExecutionResult
    }

    private suspend fun executeTask(): TaskResult<R?>{
        val thisAsAny = this.safeCast<ManagedTask<Any?>>().getOrThrow(DefaultException("ThisAsAny cast failed"))
        runCatching {
            withContext(coroutineContext) {
                info("Starting")
                parent?.let {withParent->
                    val result = block.invoke(thisAsAny, helper)
                    stopTimer()
                    taskResult.provideResult(elapsed, result)
                    taskResult.provideChildResults(childTasks.map {it.taskResult})
                    info("Complete")
                }?:run{
                    val result = block.invoke(thisAsAny, helper)
                    stopTimer()
                    taskResult.provideResult(elapsed, result)
                    taskResult.provideChildResults(childTasks.map {it.taskResult})
                    info("Complete")
                }
            }
        }.getOrElse {
            stopTimer()
            handleException(it)
            taskResult.provideThrowable(elapsed ,it)
            taskResult.provideChildResults(childTasks.map {it.taskResult})
            return taskResult
        }
        return taskResult
    }

    fun echo(message: String) = notifier.echo(taskResult, message)
    fun info(message: String) = notifier.info(taskResult,message)
    fun warn(message: String) = notifier.warn(taskResult,message)
    fun error(ex: Throwable, optMessage: String = "") = notifier.error(taskResult, ex, optMessage)
}