package po.misc.callbacks.components

import po.misc.callbacks.CallbackPayloadBase

class PayloadAnalyzer<E: Enum<E>>(
    private val payload: CallbackPayloadBase<E,* , *>
) {
    data class PayloadStats(
        val subscriptionCount: Int,
        val leftoverDataCount: Int,
        val routedContainersCount: Int
    )

    data class SubscriptionInfo(
        val inRouted: Boolean,
        val available: Boolean,
        val isListen: Boolean,
        val eventRegistered: Boolean,
    )

    fun getStats():PayloadStats{
       return PayloadStats(
          subscriptionCount = payload.containers.size,
          leftoverDataCount = payload.leftoverData.size,
          routedContainersCount = payload.routedContainers.size
        )
    }

    fun analyze(){
//        val result: MutableList<SubscriptionInfo> = mutableListOf()
//        getPayload<T>(eventType)?.let {
//            it.containers.forEach { container ->
//                container.value.expires
//                result.add(SubscriptionInfo(false, true, !container.value.expires, true))
//            }
//            if (result.isEmpty()) {
//                result.add(SubscriptionInfo(false, false, false, true))
//            }
//        } ?: result.add(SubscriptionInfo(false, false, false, false))
//        return result
    }
}