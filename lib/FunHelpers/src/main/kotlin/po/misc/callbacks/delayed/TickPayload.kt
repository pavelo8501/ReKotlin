package po.misc.callbacks.delayed

import po.misc.types.k_class.simpleOrAnon
import kotlin.time.Duration


data class TickPayload<T: Any>(
    val tick: Int,
    val ticksTotal: Int,
    val delayValue: Duration,
    val value: T
) {
    val ticksLeft: Int get() = ticksTotal - tick
    override fun toString(): String  = "Tick<${value::class.simpleOrAnon}>[${tick}/${ticksTotal} delayed by ${delayValue}]"

}