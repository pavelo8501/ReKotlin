package po.misc.debugging

import po.misc.context.CTX
import po.misc.exceptions.models.StackFrameMeta

data class DebugFrame(
    val inContext: CTX,
    val frameMeta: List<StackFrameMeta>
)
