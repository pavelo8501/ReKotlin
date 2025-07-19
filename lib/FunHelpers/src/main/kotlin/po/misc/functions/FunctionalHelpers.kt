package po.misc.functions

import po.misc.exceptions.ManagedException
import po.misc.functions.models.RepeatAttempts
import po.misc.functions.models.RepeatResult


/**
 * Executes the given [block] repeatedly if it throws an exception, up to a specified number of [times].
 *
 * This function is useful for retrying fragile or fault-prone operations such as I/O,
 * parsing, or network calls that may occasionally fail.
 *
 * Optionally, an [actionOnFault] can be provided to perform a delay, log a warning,
 * or implement backoff logic between retry attempts.
 *
 * @param times the maximum number of retry attempts. Use `0` to retry indefinitely.
 * @param actionOnFault an optional side-effect to run after each failed attempt (e.g., `Thread.sleep`, logging).
 * @param block the action to perform, taking the current [attempt] number (starting from 1).
 * @return the successful result of [block] if it completes without exception.
 * @throws Throwable the last caught exception if all attempts fail.
 */

inline fun <R : Any?> repeatIfFaulty(
    times: Int = 1,
    crossinline block: (RepeatAttempts) -> R
): R {

    val resultData = RepeatResult<R>()
    val effectiveTimes = times.coerceAtLeast(1)

    var attempt = 1
    var throwable: Throwable? = null
    val data = RepeatAttempts(attemptsTotal = effectiveTimes)
    data.recalculate(attempt, effectiveTimes)
    while (attempt <= effectiveTimes) {
        try {
            val result = block(data)
            return result
        } catch (ex: Throwable) {
            attempt++
            data.recalculate(attempt, effectiveTimes)
            throwable = ex
            resultData.addException(ex)
        }
    }
    throw throwable?: ManagedException("No result no Exception")
}

/**
 * Executes the given suspending [block] repeatedly if it throws an exception, up to a specified number of [times].
 *
 * Instead of passing the current attempt number, this version passes the number of [attemptsLeft] to the [block],
 * allowing the operation to adapt its behavior depending on how many retries remain.
 *
 * @param times the maximum number of retry attempts. Use `0` to retry indefinitely.
 * @param actionOnFault an optional suspending function invoked after each failed attempt.
 * @param block the suspending operation to perform, receiving a [RepeatAttempts] object
 *        where `currentAttempt` starts at 1, and `attemptsLeft` counts down to 0.
 * @return the result of [block] if successful.
 * @throws Throwable the last caught exception if all attempts fail.
 */
suspend inline fun <R> repeatIfFaultySuspending(
    times: Int = 0,
    noinline actionOnFault: (suspend (Throwable) -> Unit),
    crossinline block: suspend (RepeatAttempts) -> R
): RepeatResult<R> {
    val resultData = RepeatResult<R>()
    val effectiveTimes = times.coerceAtLeast(0)
    var attempt = 1
    val maxAttempts = effectiveTimes + 1
    val data = RepeatAttempts(attemptsTotal = effectiveTimes)
    while (attempt <= maxAttempts) {
        try {
            return resultData.provideResult(block(data))
        } catch (ex: Throwable) {
            resultData.addException(ex)
            attempt++
            data.recalculate(attempt, effectiveTimes)
            actionOnFault(ex)
        }
    }
    return resultData
}




