package po.lognotify.classes.task.result

import po.lognotify.classes.task.TaskBase
import po.misc.exceptions.ManagedException
import po.misc.exceptions.name

class TaskResult<R : Any?>(
    @PublishedApi internal val task: TaskBase<*, R>,
    var result: R,
    var throwable: ManagedException? = null
){

    val taskName: String get () = task.key.taskName

    @Suppress("UNCHECKED_CAST")
    constructor(task: TaskBase<*, R>, throwable: ManagedException): this(task, null as R, throwable){
        provideThrowable(throwable)
    }

    private val personalName: String = "TaskResult"

    var isSuccess : Boolean = true

    val isResult: Boolean
        get(){ return result != null }

    val hasThrowable: Boolean get(){
        return throwable!=null
    }

    private fun provideResult(newResult:R):R{
        result = newResult
        return result
    }

    private var onCompleteFn: ((TaskResult<R>)->Unit)? = null
    fun onComplete(block: (TaskResult<R>)->Unit): TaskResult<R>{
        onCompleteFn = block
        block.invoke(this)
        return this
    }

    private var onResultFn: ((R) -> Unit)? = null
    fun onResult(block: (R)->Unit): TaskResult<R> {
        onResultFn = block
        if(result != null){
            block.invoke(result)
        }
        return this
    }

    private var onFailFn: ((ManagedException) -> Unit)? = null
    fun onFail(callback: (ManagedException)->Unit): TaskResult<R> {
        task.dataProcessor.debug("Handled onFail registered","${personalName}|onFail")
        throwable?.let {
            task.taskStatus = TaskBase.TaskStatus.Faulty
            task.dataProcessor.info("Handled ${it.name()} by onFail")
            callback.invoke(it)
        }
        return this
    }

    fun resultOrException():R {
        throwable?.let {
            task.dataProcessor.error(it)
            throw it
        }
        return result
    }

    private var exHandlingCallback: (suspend ()-> R)? = null
    suspend fun handleException( resultCallback: suspend ()-> R){
        exHandlingCallback = resultCallback
        throwable?.let {
            result = resultCallback()
            task.dataProcessor.debug("Faulty result handled silently", "${personalName}|handleException")
        }
    }

    private var onHandleFailure: ((ManagedException?)-> R)? = null
    fun handleFailure(handleFailureCallback: (ManagedException?)-> R):R {
        onHandleFailure = handleFailureCallback
        return throwable?.let { managed ->
            val message = "Handled ${managed.name()} by onHandleFailure. Fallback result provided"
            task.dataProcessor.warn(message)
            provideResult(handleFailureCallback.invoke(managed))
        } ?: result
    }

    internal fun provideThrowable(th: ManagedException): TaskResult<R>{
        isSuccess = false
        task.taskStatus = TaskBase.TaskStatus.Failing
        exHandlingCallback?.let {
            task.dataProcessor.debug("Faulty result handled silently", "${personalName}|handleException")
            onCompleteFn?.invoke(this)
        }?:run {
            throwable = th
            if(onFailFn != null){
                onFailFn?.invoke(th)
            }
        }
        return  this
    }
}