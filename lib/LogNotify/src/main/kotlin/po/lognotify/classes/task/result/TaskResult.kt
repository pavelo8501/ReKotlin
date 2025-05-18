package po.lognotify.classes.task.result

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.LoggerException
import po.misc.exceptions.ManagedException

class TaskResult<R : Any?>(internal val task: ResultantTask<R>){

    val taskName: String = task.key.taskName
    var executionTime: Float = 0f

    @PublishedApi  internal var result: R? = null
        private set

    @PublishedApi   internal var throwable: ManagedException? = null
        private set

    var isSuccess : Boolean = false
    val isResult: Boolean
        get(){ return result != null }
    val hasThrowable: Boolean get(){
        return throwable!=null
    }


    private fun taskCompleted(msg: String, severity : SeverityLevel){
        task.notifier.systemInfo(EventType.STOP, severity, msg)
    }

    private fun taskCompleted(th: Throwable){
        task.notifier.systemInfo(EventType.STOP, SeverityLevel.EXCEPTION)
    }

    private fun escalate(th: ManagedException): Nothing {
        task.notifier.systemInfo(EventType.STOP, SeverityLevel.EXCEPTION)
        throw th
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
            block.invoke(result!!)
        }
        return this
    }

    private var onFailFn: ((Throwable) -> Unit)? = null
    fun onFail(callback: (Throwable)->Unit): TaskResult<R> {
        if(throwable != null){
            callback.invoke(throwable!!)
        }
        return this
    }

    fun resultOrException():R {
        if(isSuccess && result != null){
            return result!!
        }
        if(throwable != null){
            throw throwable!!
        }
        throw LoggerException("Abnormal state. Both result and exception missing")
    }

    private var exHandlingCallback: (suspend ()-> R)? = null
    suspend fun handleException( resultCallback: suspend ()-> R){
        exHandlingCallback = resultCallback
        throwable?.let {
            result = resultCallback()
            task.notifier.warn("Faulty result handled silently")
        }
    }

    internal fun provideResult(executionResult: R?): TaskResult<R>{
        isSuccess = true
        result = executionResult
        if(executionResult != null){
            onResultFn?.invoke(executionResult)
        }
        onCompleteFn?.invoke(this)
        taskCompleted("Task complete", SeverityLevel.SYS_INFO)
        return this
    }

    internal fun provideThrowable(th: ManagedException): TaskResult<R>{
        exHandlingCallback?.let {
            task.notifier.warn("Faulty result handled silently")
            onCompleteFn?.invoke(this)
        }?:run {
            throwable = th
            if(onFailFn != null){
                onFailFn?.invoke(th)
                taskCompleted(th)
            }else{
                escalate(th)
            }
        }
        return  this
    }
}