package po.misc.callbacks

import po.misc.context.CTX
import po.misc.context.Identifiable

data class HopInfo(
    val emitterName: String,
    val receiverName: String,
    val subscriber: CTX,
    var dataName: String,
    val hopNr : Int,
){

    override fun toString(): String{
        val text = "Hop Number: ${hopNr} | Emitter:${emitterName} | Receiver:${receiverName} | Subscriber: ${subscriber.contextName}"
        val dataText = "Data sent: $dataName"
        return "${text}${dataText}"
    }
}