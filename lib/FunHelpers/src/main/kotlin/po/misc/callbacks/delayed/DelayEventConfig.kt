package po.misc.callbacks.delayed

import po.misc.callbacks.common.EventHost
import java.time.Instant
import kotlin.time.Duration


class DelayConfig(
    eventTime : Duration,
){
    private val creationTime = Instant.now()
    val fireTime: Instant = creationTime.plusMillis(eventTime.inWholeMilliseconds)

    // dynamic, always "time remaining"
    val fireAfter: Long
        get() = java.time.Duration.between(Instant.now(), fireTime).toMillis()

    val ticksCount: Int =  0
    val remainder: Long = 0
}

class DelayWithTicks<H, T>(
    val eventTime : Duration,
    val tickTime: Duration,
    val tickAction: suspend H.(TickPayload<T>)-> Unit
) where H: EventHost, T: Any{

    private val creationTime = Instant.now()
    val fireTime: Instant = creationTime.plusMillis(eventTime.inWholeMilliseconds)

    val ticksCount: Int
        get() {
            return (eventTime.inWholeMilliseconds / tickTime.inWholeMilliseconds)
                .toInt()
                .coerceAtLeast(0)
        }

    val remainder: Long get() {
        return eventTime.inWholeMilliseconds % tickTime.inWholeMilliseconds
    }

    val fireAfter: Long
        get() = java.time.Duration.between(Instant.now(), fireTime).toMillis()

}