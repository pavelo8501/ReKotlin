package po.lognotify.classes.task.result

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.TaskBase
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.LoggerException
import po.misc.exceptions.ManagedException
import po.misc.exceptions.exceptionName

class TaskResult<R : Any?>(
    internal val task: TaskBase<*, R>,
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
    private fun taskCompleted(th: Throwable){
        task.notifier.systemInfo(EventType.STOP, SeverityLevel.EXCEPTION)
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
        task.dataProcessor.debug("Handled onFail registered","${personalName}|onFail",task)
        throwable?.let {
            task.taskStatus = TaskBase.TaskStatus.Faulty
            task.dataProcessor.info("Handled ${it.exceptionName()} by onFail", task)
            callback.invoke(it)
        }
        return this
    }

    fun resultOrException():R {
        throwable?.let {
            task.dataProcessor.error("ResultOrException triggered. Escalating ${it.exceptionName()}", task)
            throw it
        }
        return result
    }

    private var exHandlingCallback: (suspend ()-> R)? = null
    suspend fun handleException( resultCallback: suspend ()-> R){
        exHandlingCallback = resultCallback
        throwable?.let {
            result = resultCallback()
            task.notifier.warn("Faulty result handled silently")
        }
    }

    private var onHandleFailure: ((ManagedException?)-> R)? = null
    fun handleFailure(handleFailureCallback: (ManagedException?)-> R):R {
        onHandleFailure = handleFailureCallback
        return throwable?.let { managed ->
            val message = "Handled ${managed.exceptionName()} by onHandleFailure. Fallback result provided"
            task.dataProcessor.warn(message, task)
            provideResult(handleFailureCallback.invoke(managed))
        } ?: result
    }

//    internal fun onResult(): TaskResult<R>{
//        isSuccess = true
//        onResultFn?.invoke(result)
//        onCompleteFn?.invoke(this)
//        return this
//    }

    internal fun provideThrowable(th: ManagedException): TaskResult<R>{
        isSuccess = false
        task.taskStatus = TaskBase.TaskStatus.Failing
        exHandlingCallback?.let {
            task.notifier.warn("Faulty result handled silently")
            onCompleteFn?.invoke(this)
        }?:run {
            throwable = th
            if(onFailFn != null){
                onFailFn?.invoke(th)
                taskCompleted(th)
            }
        }
        return  this
    }
}