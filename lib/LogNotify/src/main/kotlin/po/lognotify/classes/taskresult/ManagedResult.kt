package po.lognotify.classes.taskresult

import po.lognotify.exceptions.ExceptionBase
import po.lognotify.models.LogRecord

interface ManagedResult<R : Any?>{
    val taskName: String
    val executionTime: Float
    var isSuccess : Boolean

    var resultContext: (suspend TaskResult<R>.(value: R)-> Unit)?

    fun isResult(): Boolean

    suspend fun onSuccess(block: suspend (ManagedResult<R>) -> Unit)
    suspend fun onResult(block: suspend (R) -> Unit):ManagedResult<R>
    suspend fun onFail(block: suspend (Throwable) -> Unit):ManagedResult<R>
    suspend fun onComplete(block: suspend (ManagedResult<R>) -> Unit):ManagedResult<R>
    fun resultOrException(message: String = "", callback:((msg: String)-> ExceptionBase)? = null):R

    fun getLogRecords(cumulative: Boolean = false): List<LogRecord>

    fun printLog(withIndention : Boolean = true)
}