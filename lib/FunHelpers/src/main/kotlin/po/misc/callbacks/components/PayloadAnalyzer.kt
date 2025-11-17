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
}