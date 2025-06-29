package po.misc.functions


inline fun <R: Any> repeatIfFaulty(times: Int = 0, noinline actionOnFault: (()-> Unit)? = null,   block: (attempt: Int) -> R):R {
    var attempts = 1
    val effectiveTimes = if(times > 0){
        times
    }else{
        0
    }
    var lastError: Throwable? = null
    while (attempts <= effectiveTimes || effectiveTimes == 0) {
        try {
            return block(attempts)
        } catch (ex: Throwable) {
            lastError = ex
            attempts++
            actionOnFault?.invoke()
        }
    }
    throw lastError ?: IllegalStateException("Unknown error")
}