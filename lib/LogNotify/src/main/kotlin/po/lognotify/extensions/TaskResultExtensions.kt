package po.lognotify.extensions

import po.lognotify.classes.taskresult.ManagedResult
import po.lognotify.classes.taskresult.TaskResult
import po.lognotify.exceptions.LoggerException


fun <R> ManagedResult<R>.resultOrNull(): R? = try {
    this.resultOrException<LoggerException>()
} catch (_: Throwable) {
    null
}

fun <R> ManagedResult<R>.throwIfFailed(): ManagedResult<R> {
    if (!isSuccess) throw IllegalStateException("Task [${taskName}] failed or no result available.")
    return this
}

inline fun <R> ManagedResult<R>.onSuccessResult(block: (R) -> Unit): ManagedResult<R> {
    val value =  this.resultOrException<LoggerException>()
    if (isSuccess && value != null) block(value)
    return this
}

inline fun <R> ManagedResult<R>.onSuccessValue(block: (R) -> Unit): ManagedResult<R> {
    val value =  this.resultOrException<LoggerException>()
    if (isSuccess && value != null) block(value)
    return this
}

inline fun <R> ManagedResult<R>.onFailureCause(block: (Throwable?) -> Unit): ManagedResult<R> {
    if (!isSuccess) block(
        runCatching {
            this.resultOrException<LoggerException>()
        }.exceptionOrNull())
    return this
}



fun <R> ManagedResult<R>.resultOrDefault(defaultValue: R):R {

   val result = try {
        this.resultOrException<LoggerException>()
    }catch (th: Throwable){
       defaultValue
    }
    return result
}

fun <R> ManagedResult<R>.toKotlinResult(): Result<R> =
    runCatching {  this.resultOrException<LoggerException>() }



