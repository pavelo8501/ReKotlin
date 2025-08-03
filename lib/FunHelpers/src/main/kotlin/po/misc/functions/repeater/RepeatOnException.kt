package po.misc.functions.repeater

import po.misc.functions.repeater.anotations.RepeatDsl
import po.misc.functions.repeater.models.RepeatStats
import po.misc.functions.repeater.models.RepeaterConfig



/**
 * Executes the given [block] and retries it on exception based on the provided [config].
 *
 * The function will attempt to invoke [block] up to [config.repeat] times (minimum 0).
 * If the block throws an exception, it is registered in [RepeatStats], and optionally
 * handled via [RepeaterConfig.onException]. If all retries fail, the last thrown
 * exception is rethrown.
 *
 * @param config The configuration for the repeat behavior, including retry count and optional exception handler.
 * @param block The action to be executed, potentially retried on failure.
 * @return The result of a successful [block] execution.
 * @throws Throwable The last exception encountered if all retry attempts fail.
 */


fun <R> repeatOnFault(
    config:RepeaterConfig.()-> Unit,
    block:()->R
):R{
    val finalConfig = RepeaterConfig().apply(config)
    val repeatStats = RepeatStats(finalConfig)
    while (repeatStats.attempt <= repeatStats.maxAttempts){
        try {
            return block.invoke()
        }catch (th: Throwable){
            repeatStats.registerException(th)
        }
    }
    throw repeatStats.exception
}


/**
 * Executes the given suspending [block] and retries it on exception based on the provided [config].
 *
 * The function will attempt to invoke [block] up to [config.repeat] times (minimum 0).
 * If the block throws an exception, it is registered in [RepeatStats], and optionally
 * handled via [RepeaterConfig.onException]. If all retries fail, the last thrown
 * exception is rethrown.
 *
 * @param config The configuration for the repeat behavior, including retry count and optional exception handler.
 * @param block The action to be executed, potentially retried on failure.
 * @return The result of a successful [block] execution.
 * @throws Throwable The last exception encountered if all retry attempts fail.
 */
suspend fun <R> repeatOnFaultSuspending(
    config: RepeaterConfig,
    block:suspend ()->R
):R?{
    val repeatStats = RepeatStats(config)
    while (repeatStats.attempt < repeatStats.maxAttempts){
        try {
            return block.invoke()
        }catch (th: Throwable){
            repeatStats.registerException(th)
        }
    }
    throw repeatStats.exception
}