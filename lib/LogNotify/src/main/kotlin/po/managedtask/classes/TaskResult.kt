package po.managedtask.classes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import po.managedtask.enums.SeverityLevel
import po.managedtask.helpers.StaticsHelper
import po.managedtask.models.LogRecord


interface ManagedResult<R: Any?>{

        val taskName: String
        val executionTime: Float

        suspend fun onSuccess(block: suspend (TaskResult<R>) -> Unit): TaskResult<R>
        suspend fun onFailure(block: suspend (TaskResult<R>) -> Unit): TaskResult<R>
        suspend fun onComplete(block: suspend (List<LogRecord>) -> Unit): TaskResult<R>

        fun getLogRecords(cumulative: Boolean = false): List<LogRecord>

        fun printLog(withIndention : Boolean = true)

        fun addInfo(message: String)
        fun addWarning(message: String)
        fun addException(message : String)
    }

    class TaskResult<R: Any?>(
        override val taskName: String,
    ): ManagedResult<R>
    {
        private val helper = StaticsHelper(taskName)

        private var executionTimeComputed: Float = 0f
        override val executionTime: Float
            get () =  executionTimeComputed

        internal var executionResult: R? = null
        private var throwable: Throwable? = null

        private val logs = mutableListOf<LogRecord>()
        private val childResults = mutableListOf<TaskResult<*>>()

        private var isCompleted = false
        internal var onSuccess: (suspend (TaskResult<R>) -> Unit)? = null
        private var  isThrowableCompleted = false
        internal var onFailure: (suspend (TaskResult<R>) -> Unit)? = null
        internal var onComplete: (suspend (List<LogRecord>) -> Unit)? = null

        internal suspend fun provideResult(time: Float, result: R){
            executionTimeComputed = time
            executionResult = result
            isCompleted = true
            onSuccess?.invoke(this)
            onComplete?.invoke(getLogRecords())
        }
        internal suspend fun provideThrowable(time: Float, th: Throwable){
            executionTimeComputed = time
            throwable = th
            isThrowableCompleted = true
            onFailure?.invoke(this)
            onComplete?.invoke(getLogRecords())
        }
        internal suspend fun provideChildResults(results : List<TaskResult<*>>){
            childResults.addAll(results)
        }

        override fun addInfo(message: String){
            logs.add(LogRecord(taskName,  message.trim(), SeverityLevel.INFO))
        }
        override fun addWarning(message: String){
            logs.add(LogRecord(taskName, message.trim(), SeverityLevel.WARNING))
        }
        override fun addException(message : String){
            logs.add(LogRecord(taskName, message.trim(), SeverityLevel.EXCEPTION))
        }

        override suspend fun onSuccess(block: suspend (TaskResult<R>) -> Unit): TaskResult<R> {
            onSuccess = block
            if (isCompleted) {
                block(this)
            }
            return this
        }

        override suspend fun onFailure(block: suspend (TaskResult<R>) -> Unit): TaskResult<R> {
            onFailure = block
            CoroutineScope(Dispatchers.Default).launch {
                block(this@TaskResult)
            }
            return this
        }

        override suspend fun onComplete(block: suspend (List<LogRecord>) -> Unit): TaskResult<R> {
            onComplete = block
            CoroutineScope(Dispatchers.Default).launch {
                block(getLogRecords())
            }
            return this
        }

        fun extractResult(): R? = executionResult
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