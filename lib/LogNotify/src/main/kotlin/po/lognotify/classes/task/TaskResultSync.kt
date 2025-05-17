package po.lognotify.classes.task

import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.LoggerException
import po.misc.exceptions.ManagedException

class TaskResultSync<R : Any?>(internal val task: TaskBaseSync<R>){
    var result : R? = null

    val taskName: String = task.key.taskName
    var executionTime: Float = 0f

    private var value: R? = null
    private var throwable: ManagedException? = null

    //Means that result was provided (Nullable or not)
    var isSuccess: Boolean = false
        private set

    val isResult: Boolean
        get(){ return value != null }

    internal var onCompleteFn: ((TaskResultSync<R>) -> Unit)? = null
    internal var onResultFn: ((R) -> Unit)? = null
    internal var onFailFn: ((Throwable) -> Unit)? = null


    fun provideResult(executionResult: R?): TaskResultSync<R>{
        isSuccess = true

        value = executionResult
        onResultFn?.invoke(value!!)
        onCompleteFn?.invoke(this)
        return this
    }
    fun provideThrowable(th: ManagedException?):TaskResultSync<R>{
        if(th != null) {
            throwable = th
            onCompleteFn?.invoke(this)
        }else{
            onCompleteFn?.invoke(this)
        }
        return this
    }

    fun resultOrException():R {

        if(throwable == null &&  value == null && !isSuccess){
            throw LoggerException("Abnormal state in Result")
        }

        if(throwable != null && value == null){
            throw throwable!!
        }else{
            return value!!
        }
    }

    fun onComplete(block: (TaskResultSync<R>) -> Unit):TaskResultSync<R>{
        onCompleteFn = block
        block.invoke(this)
        return this
    }

    fun onResult(block: (R) -> Unit): TaskResultSync<R> {
        onResultFn = block
        if(value != null){
            block.invoke(value!!)
        }
        return this
    }


}