package po.misc.callbacks.models


import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

data class SubscriptionRecord(
    val identity: Identifiable,
    val eventType: ValueBased
)

data class ManagerStats(val newEntry: SubscriptionRecord, val registrySize: Int, val ownName: String)

data class TriggerEvent<T: Any>(val id: Identifiable, val type: ValueBased, val value: T, val ownName: String)

data class PostTriggerEvent(val triggeredCount: Int, val registrySize: Int, val ownName: String)


