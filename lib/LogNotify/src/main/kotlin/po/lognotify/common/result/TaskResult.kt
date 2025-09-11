package po.lognotify.common.result

import po.lognotify.exceptions.ActionResult
import po.lognotify.exceptions.FailAction
import po.lognotify.exceptions.FailHandlingRationale
import po.lognotify.exceptions.getOrLoggerException
import po.lognotify.notification.warning
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.TaskBase
import po.misc.context.CTX
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText
import po.misc.functions.containers.Notifier
import po.misc.functions.containers.lambdaAsNotifier
import po.misc.reflection.classes.ClassInfo

sealed interface ExecutionResult<R : Any?> {
    var isSuccess: Boolean
    var classInfo: ClassInfo<R>?

    fun provideClassInfo(info: ClassInfo<R>)
}

class TaskResult<T: CTX, R>(
    val task: TaskBase<T, R>
) : ExecutionResult<R> {

    var exception: ManagedException? = null
    var result:R? = null


    constructor(task: TaskBase<T, R>, throwable: ManagedException) : this(task) {
        provideException(throwable)
    }

    constructor(task: TaskBase<T, R>, result: R): this(task){
        this.result = result
    }

    private val personalName: String = "TaskResult"
    override var isSuccess: Boolean = true

    override var classInfo: ClassInfo<R>? = null

    val hasResult: Boolean get() = result != null
    val hasThrowable: Boolean get() = exception != null

    val hasGuardConditions: Boolean get() = !(onFailFn == null && exHandlingCallback == null)

    val  rationaleList = mutableListOf<FailHandlingRationale>()

    val collectedErrors: MutableList<Throwable> = mutableListOf()

    private var onCompleteFn: ((TaskResult<T, R>) -> Unit)? = null
    private var beforeFaultyResultRequested: Notifier<ManagedException>? = null

    @PublishedApi
    internal fun fallbackInAction(action: ActionResult){
        rationaleList.lastOrNull()?.setAction(action)
    }

    @PublishedApi
    internal fun collectThrowable(throwable: Throwable) {
        val previous = collectedErrors.lastOrNull()
        previous?.let { previous ->
            if (throwable !== previous) {
                collectedErrors.add(throwable)
            }
        }?: collectedErrors.add(throwable)
    }

    @PublishedApi
    internal fun subscribeBeforeFault(callback: (ManagedException) -> Unit) {
        //"subscribeBeforeFault call".output(Colour.CYAN)
        beforeFaultyResultRequested = lambdaAsNotifier<ManagedException>(callback)
    }

    internal fun provideResult(newResult: R): TaskResult<T, R> {
       // "provideResult call".output(Colour.CYAN)
        result = newResult
        return this
    }

    internal fun provideException(th: ManagedException): TaskResult<T, R> {
      //  "provideException call".output(Colour.CYAN)
        collectThrowable(th)
        isSuccess = false
        task.changeStatus(ExecutionStatus.Failing)
        exHandlingCallback?.let {
            task.dataProcessor.debug("Faulty result handled silently", "$personalName|handleException")
            onCompleteFn?.invoke(this)
        } ?: run {
            exception = th
            if (onFailFn != null) {
                onFailFn?.invoke(th)
            }
        }
        return this
    }

    fun onComplete(block: (TaskResult<T, R>) -> Unit): TaskResult<T, R> {
       // "onComplete call".output(Colour.CYAN)
        onCompleteFn = block
        block.invoke(this)
        return this
    }

    private var onResultFn: ((R) -> Unit)? = null

    fun onResult(block: (R) -> Unit): TaskResult<T, R> {
      //  "onResult call".output(Colour.CYAN)
        onResultFn = block
        result?.let {
            block.invoke(it)
        }
        return this
    }

    private var onFailFn: ((ManagedException) -> Unit)? = null

    fun onFail(callback: (ManagedException) -> Unit): TaskResult<T, R> {
       // "onFail callback set".output(Colour.CYAN)
        task.dataProcessor.debug("Handled onFail registered", "$personalName|onFail")
        exception?.let {
            task.changeStatus(ExecutionStatus.Faulty)
            task.warning("Handled ${it.throwableToText()} by onFail")
            callback.invoke(it)
        }
        return this
    }

    fun resultOrException(): R {
       // "resultOrException call".output(Colour.CYAN)
        val managed = exception
        if (managed != null) {
            beforeFaultyResultRequested?.trigger(managed)
            throw managed
        }
        return result.getOrLoggerException("No exception no result")
    }

    private var exHandlingCallback: (suspend () -> R)? = null

    suspend fun handleException(resultCallback: suspend () -> R) {
        exHandlingCallback = resultCallback
        exception?.let {
            result = resultCallback()
            task.dataProcessor.debug("Faulty result handled silently", "$personalName|handleException")
        }
    }

    private var onHandleFailure: ((ManagedException?) -> R)? = null

    fun handleFailure(handleFailureCallback: (ManagedException?) -> R): R {
       // "handleFailure call".output(Colour.CYAN)
        onHandleFailure = handleFailureCallback
        return exception?.let { managed ->
            val message = "Handled ${managed.throwableToText()} by onHandleFailure. Fallback result provided"
            task.warning(message)
            handleFailureCallback.invoke(managed)
        } ?: run {
            result.getOrLoggerException("No exception no result")
        }
    }

    override fun provideClassInfo(info: ClassInfo<R>) {
        classInfo = info
    }
}
