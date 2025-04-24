package po.lognotify.classes.taskresult

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.getOrThrow
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException


class TaskResult<R : Any?>(private val task: TaskSealedBase<R>): ManagedResult<R> {

    override val taskName: String = task.taskName
    override var executionTime: Float = 0f

    private var value: R? = null
    private var throwable: ManagedException? = null

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
    override fun resultOrException(exception: SelfThrownException<*>?):R {

        if(value != null){
            return value!!
        }else{
            if(exception != null){
                if(throwable!=null){
                    exception.setSourceException(throwable!!)
                }
                exception.throwSelf()
            }
            val registeredEx = throwable.getOrThrow("value is null", HandlerType.UNMANAGED)
            throw registeredEx
        }
    }
    override suspend fun setFallback(handler: HandlerType, fallbackFn: ()->R): ManagedResult<R>{
        task.taskRunner.exceptionHandler.provideHandlerFn(handler, fallbackFn)
        return this
    }

    suspend fun provideResult(time: Float, executionResult: R?){
        isSuccess = true
        executionTime = time
        value = executionResult
        taskCompleted()
        onResultFn?.invoke(value!!)
        onCompleteFn?.invoke(this as ManagedResult<R>)
    }


    suspend fun provideThrowable(time: Float, th: ManagedException?){
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