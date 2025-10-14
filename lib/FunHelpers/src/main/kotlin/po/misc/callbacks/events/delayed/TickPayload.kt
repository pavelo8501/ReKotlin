package po.misc.callbacks.events.delayed

import po.misc.types.helpers.simpleOrNan
import kotlin.time.Duration


data class TickPayload<T: Any>(
    val tick: Int,
    val ticksTotal: Int,
    val delayValue: Duration,
    val value: T
) {
    val ticksLeft: Int get() = ticksTotal - tick
    override fun toString(): String  = "Tick<${value::class.simpleOrNan()}>[${tick}/${ticksTotal} delayed by ${delayValue}]"

}