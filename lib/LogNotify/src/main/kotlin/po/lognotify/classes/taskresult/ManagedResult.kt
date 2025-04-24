package po.lognotify.classes.taskresult

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

interface ManagedResult<R : Any?>{
    val taskName: String
    val executionTime: Float
    var isSuccess : Boolean
    val isResult : Boolean

    fun onResult(block: (R) -> Unit):ManagedResult<R>
    //suspend fun onFail(block: suspend (Throwable) -> Unit):ManagedResult<R>
    suspend fun safeReturn(resultCallback: ()-> R?)
    fun resultOrException():R
    suspend fun handleFailure(vararg  handler: HandlerType, fallbackFn: suspend (exception: ManagedException)->R): ManagedResult<R>
    fun onComplete(block: (ManagedResult<R>) -> Unit):ManagedResult<R>
}