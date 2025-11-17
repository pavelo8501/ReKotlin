package po.misc.callbacks.loop

import po.misc.data.PrettyPrint
import po.misc.data.logging.Verbosity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds



class LoopConfig(
    var boostWindowSize: Int = 10,
    var requestDelay: Duration = 10.seconds,
    var verbosity: Verbosity = Verbosity.Info
)



data class LoopStats(
    var perLoopProcessedCount: Int = 0,
    var totalProcessedCount: Long = 0,
    var loopsCount: Long = 1L,
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

