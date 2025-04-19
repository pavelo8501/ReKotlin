package po.lognotify.classes.taskresult

import po.lognotify.exceptions.ManagedException

interface ManagedResult<R : Any?>{
    val taskName: String
    val executionTime: Float
    var isSuccess : Boolean

    fun isResult(): Boolean
    fun onResult(block: (R) -> Unit):ManagedResult<R>
    suspend fun onFail(block: suspend (Throwable) -> Unit):ManagedResult<R>
    fun onComplete(block: (ManagedResult<R>) -> Unit):ManagedResult<R>
    fun <E: ManagedException> resultOrException(message: String = "", callback:((msg: String)-> E)? = null):R

}