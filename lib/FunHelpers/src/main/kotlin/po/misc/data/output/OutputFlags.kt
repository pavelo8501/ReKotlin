package po.misc.data.output



sealed interface OutputBehaviour

object Identify : OutputBehaviour
object Timestamp : OutputBehaviour
object ToString : OutputBehaviour

sealed interface OutputResultBehaviour
object Pass : OutputResultBehaviour

sealed interface OutputProvider
object SyncPrint :OutputProvider
object PrintOnComplete :OutputProvider

sealed interface DebugProvider
object IdentifyIt :DebugProvider



