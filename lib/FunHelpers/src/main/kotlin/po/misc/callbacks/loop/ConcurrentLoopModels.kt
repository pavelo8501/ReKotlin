package po.misc.callbacks.loop

import po.misc.data.PrettyPrint
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class LoopConfig(
    var boostWindowSize: Int = 10,
    var requestDelay: Duration = 10.seconds
)

data class LoopStats<UPDATE: Any>(
    var lastUpdateProcessed: UPDATE? = null,
    var perLoopProcessedCount: Int = 0,
    var totalProcessedCount: Long = 0,
    var loopsCount: Long = 0L,
    var inBoostMode: Boolean = false
): PrettyPrint{

    override val formattedString: String = toString()

    override fun toString(): String {
        return  buildString {
            append("In boost mode: $inBoostMode,  Loops count: $loopsCount, Total processed: $totalProcessedCount")
            appendLine()
        }
    }

}

