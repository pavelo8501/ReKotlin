package po.misc.data.output

import po.misc.data.styles.TextStyler
import po.misc.data.text_span.TextSpan


sealed interface OutputBehaviour


object Identify : OutputBehaviour
object Timestamp : OutputBehaviour
object ToString : OutputBehaviour
object HighLight : OutputBehaviour

sealed interface OutputParameter{
    val printInlined: Boolean get() = true
}

object Time : OutputParameter

class NameHeader(val name: String, val actionTag: TextStyler.ActionTag = TextStyler.ActionTag.Speech): OutputParameter{
    constructor(span: TextSpan, actionTag: TextStyler.ActionTag = TextStyler.ActionTag.Speech): this(span.styled, actionTag)
}


sealed interface OutputResultBehaviour

object Pass: OutputResultBehaviour

sealed interface OutputProvider

object SyncPrint :OutputProvider
object PrintOnComplete :OutputProvider
object LocateOutputs : OutputProvider




sealed interface DebugProvider



