package po.misc.callbacks

import po.misc.data.styles.StylableText
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext

data class HopInfo(
    val emitterName: String,
    val receiverName: String,
    val subscriber: IdentifiableContext,
    var dataName: String,
    val hopNr : Int,
): StylableText{

    override fun toString(): String{
        val text = "Hop Number: ${hopNr} | Emitter:${emitterName} | Receiver:${receiverName} | Subscriber: ${subscriber.contextName}".newLine()
        val dataText = "Data sent: $dataName"
        return "${text}${dataText}"
    }
}