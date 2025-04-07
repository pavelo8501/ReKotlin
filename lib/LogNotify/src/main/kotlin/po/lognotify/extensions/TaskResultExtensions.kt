package po.lognotify.extensions




fun <R> ManagedResul<R>.resultOrNull(): R? = try {
    this.resultOrException()
} catch (_: Throwable) {
    null
}

fun <R> ManagedResult<R>.throwIfFailed(): ManagedResult<R> {
    if (!isSuccess) throw IllegalStateException("Task [${taskName}] failed or no result available.")
    return this
}

inline fun <R> ManagedResult<R>.onSuccessResult(block: (R) -> Unit): ManagedResult<R> {
    val value =  this.resultOrException()
    if (isSuccess && value != null) block(value)
    return this
}

inline fun <R> ManagedResult<R>.onSuccessValue(block: (R) -> Unit): ManagedResult<R> {
    val value =  this.resultOrException()
    if (isSuccess && value != null) block(value)
    return this
}

inline fun <R> ManagedResult<R>.onFailureCause(block: (Throwable?) -> Unit): ManagedResult<R> {
    if (!isSuccess) block(
        runCatching {
            this.resultOrException()
        }.exceptionOrNull())
    return this
}

fun <R> ManagedResult<R>.toKotlinResult(): Result<R> =
    runCatching {  this.resultOrException() }



