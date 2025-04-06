package po.managedtask.classes

import po.managedtask.classes.task.TaskSealedBase
import po.managedtask.enums.SeverityLevel
import po.managedtask.exceptions.DefaultException
import po.managedtask.exceptions.enums.DefaultType
import po.managedtask.models.LogRecord


interface ManagedResult<R>{
        val taskName: String
        val executionTime: Float
        var isSuccess : Boolean
        var value: R?

        var resultContext: (suspend TaskResult<R>.(value: R)-> Unit)?

        suspend fun onSuccess(block: suspend (ManagedResult<R>) -> Unit)
        suspend fun onResult(block: suspend (R) -> Unit):ManagedResult<R>
        suspend fun onFail(block: suspend (Throwable) -> Unit):ManagedResult<R>
        suspend fun onComplete(block: suspend (ManagedResult<R>) -> Unit):ManagedResult<R>

        fun getLogRecords(cumulative: Boolean = false): List<LogRecord>

        fun printLog(withIndention : Boolean = true)
    }

    class TaskResult<R>(
        private val task: TaskSealedBase<R>,
    ): ManagedResult<R>
    {
        override val taskName: String = task.key.taskName
        private val helper = task.helper
        private val taskHelper = task.notifier
        override var executionTime: Float = 0f

        override var value: R? = null
        private var throwable: Throwable? = null

        val resultHandler:R
            get() = value?:throw DefaultException("Result unavailable", DefaultType.DEFAULT)

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

        internal suspend fun provideResult(time: Float, value: R){
            isSuccess = true
            executionTime = time
            this.value = value
            onResultFn?.invoke(resultHandler)
            onCompleteFn?.invoke(this as ManagedResult<R>)
        }

        internal suspend fun provideThrowable(time: Float, th: Throwable){
            executionTime = time
            throwable = th
            onFailFn?.let {
                it.invoke(throwable!!)
                onCompleteFn?.invoke(this as ManagedResult<R>)
            }?:run {
                task.escalate(th)
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

        fun addInfo(message: String){
            logs.add(LogRecord(taskName,  message.trim(), SeverityLevel.INFO))
        }
        fun addWarning(message: String){
            logs.add(LogRecord(taskName, message.trim(), SeverityLevel.WARNING))
        }
        fun addException(message : String){
            logs.add(LogRecord(taskName, message.trim(), SeverityLevel.EXCEPTION))
        }

        fun extractResult(): R? = value
        fun getAsThrowable(): Throwable? = throwable
        fun reThrowIfAny() {
            throwable?.let { throw it }
        }

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
            helper.formatLogWithIndention(records)
        }
    }