package po.lognotify.extensions

import po.lognotify.classes.task.TaskResult

fun <R> TaskResult<R>.resultOrNull(): R? = try {
    this.resultOrException()
} catch (_: Throwable) {
    null
}

inline fun <R> TaskResult<R>.onSuccessResult(block: (R) -> Unit): TaskResult<R> {
    val value =  this.resultOrException()
    if (isSuccess && value != null) block(value)
    return this
}

inline fun <R> TaskResult<R>.onFailureCause(block: (Throwable?) -> Unit): TaskResult<R> {
    if (!isSuccess) block(
        runCatching {
            this.resultOrException()
        }.exceptionOrNull())
    return this
}

fun <R> TaskResult<R>.resultOrDefault(defaultValue: R):R {
   val result = try {
        this.resultOrException()
    }catch (th: Throwable){
       defaultValue
    }
    return result
}

fun <R> TaskResult<R>.toKotlinResult(): Result<R> =
    runCatching {  this.resultOrException() }



