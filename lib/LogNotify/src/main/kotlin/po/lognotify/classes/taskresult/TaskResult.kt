package po.lognotify.classes.taskresult

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.LoggerException
import po.lognotify.models.LogRecord


class TaskResult<R : Any?>(private val task: TaskSealedBase<R>): ManagedResult<R> {

    override val taskName: String = task.taskName
    override var executionTime: Float = 0f

    private var value: R? = null
    private var throwable: Throwable? = null

    val resultHandler:R
        get() = value?:throw LoggerException("Result unavailable")

    override var isSuccess : Boolean = false

    internal var onCompleteFn: ((ManagedResult<R>) -> Unit)? = null
    internal var onResultFn: ((R) -> Unit)? = null
    internal var onFailFn: ((Throwable) -> Unit)? = null

    override fun onComplete(block: (ManagedResult<R>) -> Unit):ManagedResult<R>{
        onCompleteFn = block
        block.invoke(this)
        return this
    }
    override fun onResult(block: (R) -> Unit): ManagedResult<R> {
        onResultFn = block
        if(value != null){
            block.invoke(resultHandler)
        }
        return this
    }
    override fun onFail(block: (Throwable) -> Unit): ManagedResult<R> {
        onFailFn = block
        if(throwable != null){
            block.invoke(throwable!!)
        }
        return this
    }

    override fun isResult(): Boolean{
        return value != null
    }
    override fun resultOrException(message: String, callback:((msg: String)-> ExceptionBase)?):R{
        return value?:run {
            throwable?.let {
                if(it is ExceptionBase){
                    throw it
                }else{
                    throw LoggerException(it.message.toString()).setSourceException(it)
                }
            }?:run {
                val defaultMessage = "Requested Value is null in ${task.taskName} TaskResult"
                if(callback!= null){
                    throw  callback.invoke("message $defaultMessage")
                }else{
                    throw LoggerException(defaultMessage)
                }
            }
        }
    }

    private suspend fun taskCompleted(th: Throwable? = null){
        if(th == null){
            task.notifier.systemInfo("Stop", EventType.STOP, SeverityLevel.INFO)
        }else{
            task.notifier.systemInfo("Stop", EventType.STOP, SeverityLevel.EXCEPTION)
        }
        task.isComplete = true
    }

    internal suspend fun provideResult(time: Float, executionResult: R?){
        isSuccess = true
        executionTime = time
        value = executionResult
        task.notifier.systemInfo("Stop", EventType.STOP, SeverityLevel.INFO)
        taskCompleted()
        onResultFn?.invoke(resultHandler)
        onCompleteFn?.invoke(this as ManagedResult<R>)
    }
    internal suspend fun provideThrowable(time: Float, th: Throwable?){
        executionTime = time
        if(th != null) {
            isSuccess = false
            throwable = th
            taskCompleted(th)
            onFailFn?.invoke(th)
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }else{
            task.notifier.systemInfo("Execution failed. No throwable provided", EventType.STOP ,SeverityLevel.WARNING)
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }
    }
}