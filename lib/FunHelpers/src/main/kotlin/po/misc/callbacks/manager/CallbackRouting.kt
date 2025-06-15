package po.misc.callbacks.manager

import po.misc.data.styles.StylableText
import po.misc.interfaces.IdentifiableClass

data class HopInfo(
    val emitterName: String,
    val receiverName: String,
    val subscriber: IdentifiableClass,
    var dataName: String,
    val hopNr : Int,
): StylableText{

    override fun toString(): String{
        val text = "Hop Number: ${hopNr} | Emitter:${emitterName} | Receiver:${receiverName} | Subscriber: ${subscriber.completeName}".newLine()
        val dataText = "Data sent: $dataName"
        return "${text}${dataText}"
    }
}