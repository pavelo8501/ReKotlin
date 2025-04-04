package po.managedtask.exceptions

interface ExceptionsThrown {
    fun throwDefaultException(message : String): Unit?
    fun throwDefaultException(ex : Throwable): Unit?
    fun throwSkipException(message : String): Unit?
    fun throwCancellationException(message : String, cancelHandler: ((ManagedExceptionBase)-> Unit)? = null): Unit?
    fun throwPropagatedException(message : String): Unit?

}