package po.misc.functions.repeater.models

import po.misc.functions.repeater.anotations.RepeatDsl
import po.misc.functions.repeater.repeatOnFault

/**
 * Configuration holder for [repeatOnFault].
 * @property repeat Number of allowed retry attempts. Negative values are coerced to 0.
 * @property onException Optional callback invoked after each failed attempt, with the current [RepeatStats].
 */

@RepeatDsl()
class RepeaterConfig(
    var attempts: Int = 0,
    var onException: ((RepeatStats) -> Unit)? = null,
) {
    internal val effectiveRepeats get() = attempts.coerceAtLeast(0)

    fun setMaxAttempts(attempts: Int): RepeaterConfig {
        this.attempts = attempts
        return this
    }

    fun onException(callback: (RepeatStats) -> Unit): RepeaterConfig {
        onException = callback
        return this
    }
}

/**
 * Tracks state and exceptions across retries for [repeatOnFault].
 * @property config The [RepeaterConfig] used to initiate the retry sequence.
 */
class RepeatStats(
    val config: RepeaterConfig,
) {
    private val exceptionsBacking: MutableList<Throwable> = mutableListOf()
    val exceptions: List<Throwable> get() = exceptionsBacking

    private val onException: ((RepeatStats) -> Unit)? = config.onException
    val maxAttempts: Int = config.effectiveRepeats
    val attempt: Int get() = exceptionsBacking.size

    val isLastAttempt: Boolean get() = maxAttempts <= attempt

    val exception: Throwable get() = exceptions.lastOrNull()?:throw IllegalArgumentException("repeatOnFault has no exceptions registered nor has result")

    fun registerException(exception: Throwable): RepeatStats {
        onException?.let { callback ->
            exceptionsBacking.add(exception)
            callback.invoke(this)
        } ?: run {
            exceptionsBacking.add(exception)
        }
        return this
    }
}
