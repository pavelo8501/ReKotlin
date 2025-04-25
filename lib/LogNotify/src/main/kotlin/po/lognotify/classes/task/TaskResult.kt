package po.lognotify.classes.task

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.enums.SeverityLevel
import po.misc.exceptions.ManagedException

class TaskResult<R : Any?>(private val task: TaskSealedBase<R>){

    val taskName: String = task.taskData.taskName
    var executionTime: Float = 0f

    private var value: R? = null
    private var throwable: ManagedException? = null

    val isResult: Boolean
        get(){ return value != null }

    private suspend fun taskCompleted(th: Throwable? = null){
        if(th == null){
            task.notifier.systemInfo(EventType.STOP, SeverityLevel.INFO)
        }else{
            task.notifier.systemInfo(EventType.STOP, SeverityLevel.EXCEPTION)
        }
        task.notifyComplete()
    }
    private suspend fun taskCompleted(msg: String, severity : SeverityLevel){
        task.notifier.systemInfo(EventType.STOP, severity, msg)
        task.notifyComplete()
    }

    var isSuccess : Boolean = false

    internal var onCompleteFn: ((TaskResult<R>) -> Unit)? = null
    internal var onResultFn: ((R) -> Unit)? = null
    internal var onFailFn: ( suspend (Throwable) -> Unit)? = null

    fun onComplete(block: (TaskResult<R>) -> Unit):TaskResult<R>{
        onCompleteFn = block
        block.invoke(this)
        return this
    }

    fun onResult(block: (R) -> Unit): TaskResult<R> {
        onResultFn = block
        if(value != null){
            block.invoke(value!!)
        }
        return this
    }

    fun resultOrException():R {
        if(throwable != null && value == null){
            println("Prishel za rezultatom a tut takoe")
            throw throwable!!
        }else{
            return value!!
        }
    }
    private var safeReturnCallback: (()-> R?)? = null
    suspend fun safeReturn(resultCallback: ()-> R?){
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
        onCompleteFn?.invoke(this)
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
            onCompleteFn?.invoke(this)
        }else{
            taskCompleted("Execution failed. No throwable provided", SeverityLevel.WARNING)
            onCompleteFn?.invoke(this)
        }
    }
}