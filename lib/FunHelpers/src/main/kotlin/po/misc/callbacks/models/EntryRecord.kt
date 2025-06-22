package po.misc.callbacks.models


import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

data class SubscriptionRecordDepr(
    val identity: Identifiable,
    val eventType: ValueBased
)

data class ManagerStatsDepr(val newEntry: SubscriptionRecordDepr, val registrySize: Int, val ownName: String)

data class TriggerEventDepr<T: Any>(val id: Identifiable, val type: ValueBased, val value: T, val ownName: String)

data class PostTriggerEventDepr(val triggeredCount: Int, val registrySize: Int, val ownName: String)


