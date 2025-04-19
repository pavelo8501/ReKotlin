package po.lognotify.classes.taskresult

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.LoggerException


class TaskResult<R : Any?>(private val task: TaskSealedBase<R>): ManagedResult<R> {

    override val taskName: String = task.taskName
    override var executionTime: Float = 0f

    private var value: R? = null
    private var throwable: Throwable? = null

//    val resultHandler:R
//        get() = value?:throw LoggerException("Result unavailable")

    override var isSuccess : Boolean = false

    internal var onCompleteFn: ((ManagedResult<R>) -> Unit)? = null
    internal var onResultFn: ((R) -> Unit)? = null
    internal var onFailFn: ( suspend (Throwable) -> Unit)? = null

    override fun onComplete(block: (ManagedResult<R>) -> Unit):ManagedResult<R>{
        onCompleteFn = block
        block.invoke(this)
        return this
    }

    override fun onResult(block: (R) -> Unit): ManagedResult<R> {
        onResultFn = block
        if(value != null){
            block.invoke(value!!)
        }
        return this
    }
    override suspend fun onFail(block: suspend (Throwable) -> Unit): ManagedResult<R> {
        onFailFn = block
        if(throwable != null){
            block.invoke(throwable!!)
        }
        return this
    }

    override fun isResult(): Boolean{
        return value != null
    }


    override fun <E: ManagedException> resultOrException(
        message: String,
        callback:((msg: String)-> E)?
    ):R
    {
        if(value!=null){
            return value!!
        }else{
            val registeredThrowable =  throwable

            if(registeredThrowable is ManagedException){
                throw registeredThrowable
            }

            val managedException =  if(callback != null){
                callback.invoke(message)
            }else{
                LoggerException("Requested Value is null in ${task.taskName} TaskResult")
            }
            if(registeredThrowable != null){
                managedException.setSourceException(registeredThrowable)
            }
            throw managedException
        }
    }

    private suspend fun taskCompleted(th: Throwable? = null){
        if(th == null){
            task.notifier.systemInfo(EventType.STOP, SeverityLevel.INFO)
        }else{
            task.notifier.systemInfo(EventType.STOP, SeverityLevel.EXCEPTION)
        }
        task.isComplete = true
    }

    private suspend fun taskCompleted(msg: String, severity : SeverityLevel){
        task.notifier.systemInfo(EventType.STOP, severity, msg)
        task.isComplete = true
    }



    internal suspend fun provideResult(time: Float, executionResult: R?){
        isSuccess = true
        executionTime = time
        value = executionResult
        taskCompleted()
        onResultFn?.invoke(value!!)
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
            taskCompleted("Execution failed. No throwable provided", SeverityLevel.WARNING)
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }
    }
}