package po.lognotify.classes.taskresult

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.LoggerException
import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.models.LogRecord


class TaskResult<R : Any?>(private val task: TaskSealedBase<R>): ManagedResult<R> {

    override val taskName: String = task.taskName
    override var executionTime: Float = 0f

    private var value: R? = null
    private var throwable: Throwable? = null

    val resultHandler:R
        get() = value?:throw LoggerException("Result unavailable")

    override var isSuccess : Boolean = false

    internal var onCompleteFn: (suspend (ManagedResult<R>) -> Unit)? = null
    internal var onResultFn: (suspend (R) -> Unit)? = null
    internal var onFailFn: (suspend (Throwable) -> Unit)? = null

    override suspend fun onResult(block: suspend (R) -> Unit):ManagedResult<R>{
        onResultFn = block
        if(value != null){
            block.invoke(resultHandler)
        }
        return this
    }
    override suspend fun onComplete(block: suspend (ManagedResult<R>) -> Unit):ManagedResult<R>{
        onCompleteFn = block
        block.invoke(this)
        return this
    }
    override suspend fun onFail(block: suspend (Throwable) -> Unit):ManagedResult<R>{
        onFailFn = block
        if(throwable != null){
            block.invoke(throwable!!)
        }
        return this
    }

    internal suspend fun provideResult(time: Float, executionResult: R?){
        isSuccess = true
        executionTime = time
        value = executionResult
        task.notifier.systemInfo("Stop", EventType.STOP, SeverityLevel.INFO)
        onResultFn?.invoke(resultHandler)
        onCompleteFn?.invoke(this as ManagedResult<R>)
    }
    internal suspend fun provideThrowable(time: Float, th: Throwable?){
        executionTime = time
        if(th != null) {
            isSuccess = false
            throwable = th
            task.notifier.systemInfo(th.message.toString(),  EventType.STOP, SeverityLevel.WARNING)
            onFailFn?.invoke(th)
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }else{
            task.notifier.systemInfo("Execution failed. No throwable provided", EventType.STOP ,SeverityLevel.WARNING)
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }
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

    override var resultContext: (suspend TaskResult<R>.(value: R)-> Unit)? = null
    private val logs = mutableListOf<LogRecord>()
    private val childResults = mutableListOf<ManagedResult<*>>()

    suspend fun resultContextInvocator(){
        if(value != null){
            resultContext?.invoke(this, value!!)
        }
    }
    private var successBlock : (suspend (ManagedResult<R>)-> Unit)? = null
    override suspend fun onSuccess(block: suspend (ManagedResult<R>) -> Unit){
        if(value != null){
            block.invoke(this as ManagedResult<R>)
        }else{
            successBlock = block
        }
        resultContextInvocator()
    }


//    fun addInfo(message: String){
//        logs.add(LogRecord(taskName,  message.trim(), SeverityLevel.INFO))
//    }
//    fun addWarning(message: String){
//        logs.add(LogRecord(taskName, message.trim(), SeverityLevel.WARNING))
//    }
//    fun addException(message : String){
//        logs.add(LogRecord(taskName, message.trim(), SeverityLevel.EXCEPTION))
//    }
//
//    fun extractResult(): R? = value
//    fun getAsThrowable(): Throwable? = throwable
//    fun reThrowIfAny() {
//        throwable?.let { throw it }
//    }

    override fun getLogRecords(cumulative: Boolean): List<LogRecord>{
        if(!cumulative){
            return logs
        }else{
            val logRecords = mutableListOf<LogRecord>()
            logRecords.addAll(logs)
            logRecords.addAll(childResults.flatMap { it.getLogRecords(true) } )
            return logs
        }
    }

    override fun printLog(withIndention: Boolean){
        val records = getLogRecords(true)
       // helper.formatLogWithIndention(records)
    }
}