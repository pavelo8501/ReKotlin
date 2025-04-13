package po.lognotify.classes.taskresult

import po.lognotify.exceptions.ExceptionBase
import po.lognotify.models.LogRecord

interface ManagedResult<R : Any?>{
    val taskName: String
    val executionTime: Float
    var isSuccess : Boolean

    fun isResult(): Boolean
    fun onResult(block: (R) -> Unit):ManagedResult<R>
    fun onFail(block: (Throwable) -> Unit):ManagedResult<R>
    fun onComplete(block: (ManagedResult<R>) -> Unit):ManagedResult<R>
    fun resultOrException(message: String = "", callback:((msg: String)-> ExceptionBase)? = null):R

}