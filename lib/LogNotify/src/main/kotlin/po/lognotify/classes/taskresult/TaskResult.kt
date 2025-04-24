package po.lognotify.classes.taskresult

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.LoggerException
import po.lognotify.exceptions.getOrThrow
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.exceptions.getOrException


class TaskResult<R : Any?>(private val task: TaskSealedBase<R>): ManagedResult<R> {

    override val taskName: String = task.taskName
    override var executionTime: Float = 0f

    private var value: R? = null
    private var throwable: ManagedException? = null

    override val isResult: Boolean
        get(){ return value != null }

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

    override fun resultOrException():R {
        if(throwable != null && value == null){
            println("Prishel za rezultatom a tut takoe")
            throw throwable!!
        }else{
            return value!!
        }
    }

    override suspend fun handleFailure(vararg  handler: HandlerType, fallbackFn: suspend (exception: ManagedException)->R): ManagedResult<R>{
        println("Poluchil handleri")
        task.taskRunner.exceptionHandler.provideHandlerFn(handler.toSet(), fallbackFn)
        return this
    }

    private var safeReturnCallback: (()-> R?)? = null
    override suspend fun safeReturn(resultCallback: ()-> R?){
        safeReturnCallback = resultCallback
        if(throwable != null){
            task.notifier.warn("Faulty result handled silently")
            value =  safeReturnCallback!!()
        }
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
            println("Ustanavlivaju oshibku")
           // task.taskHandler.
            taskCompleted(th)
            if(safeReturnCallback == null){
                onFailFn?.invoke(th)
            }else{
                task.notifier.warn("Faulty result handled silently")
                value = safeReturnCallback!!.invoke()
            }
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }else{
            taskCompleted("Execution failed. No throwable provided", SeverityLevel.WARNING)
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }
    }
}