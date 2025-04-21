package po.lognotify.classes.taskresult

import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.SelfThrownException
import po.lognotify.exceptions.enums.HandlerType

interface ManagedResult<R : Any?>{
    val taskName: String
    val executionTime: Float
    var isSuccess : Boolean

    fun isResult(): Boolean
    fun onResult(block: (R) -> Unit):ManagedResult<R>
    suspend fun onFail(block: suspend (Throwable) -> Unit):ManagedResult<R>
    fun onComplete(block: (ManagedResult<R>) -> Unit):ManagedResult<R>
    fun  resultOrException(exception: SelfThrownException? = null):R
    suspend fun setFallback(handler: HandlerType, fallbackFn: ()->R): ManagedResult<R>
}