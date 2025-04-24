package po.lognotify.classes.taskresult

import po.misc.exceptions.HandlerType
import po.misc.exceptions.SelfThrownException

interface ManagedResult<R : Any?>{
    val taskName: String
    val executionTime: Float
    var isSuccess : Boolean

    fun isResult(): Boolean
    fun onResult(block: (R) -> Unit):ManagedResult<R>
    suspend fun onFail(block: suspend (Throwable) -> Unit):ManagedResult<R>
    fun onComplete(block: (ManagedResult<R>) -> Unit):ManagedResult<R>
    fun  resultOrException(exception: SelfThrownException<*>? = null):R
    suspend fun setFallback(handler: HandlerType, fallbackFn: ()->R): ManagedResult<R>
}