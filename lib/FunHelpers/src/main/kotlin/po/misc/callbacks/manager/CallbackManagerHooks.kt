package po.misc.callbacks.manager

import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext


class CallbackManagerHooks{

    data class SubscriptionRecord(val subscriber: IdentifiableClass, val eventType: Enum<*>)
    data class ManagerStats(val newEntry: SubscriptionRecord, val registrySize: Int, val emitter: IdentifiableContext)
    data class TriggerEvent(val subscriber: IdentifiableClass, val eventType: Enum<*>, val value: Any, val emitter: IdentifiableContext)
    data class PostTriggerEvent(val triggeredCount: Int, val registrySize: Int, val emitter: IdentifiableContext)

    internal var onNewSubscription: ((ManagerStats)-> Unit)? = null
    internal var onBeforeTrigger: ((TriggerEvent)-> Unit)? = null
    internal var onAfterTriggered: ((PostTriggerEvent)-> Unit)? = null

    internal var onForwarding: ((HopInfo)-> Unit)? = null

    fun forwarding(hook: (HopInfo) -> Unit){
        onForwarding = hook
    }

    fun newSubscription(hook: (ManagerStats) -> Unit) {
        onNewSubscription = hook
    }

    fun beforeTrigger(hook: (TriggerEvent)-> Unit) {
        onBeforeTrigger = hook
    }

    fun afterTriggered(hook: (PostTriggerEvent)-> Unit) {
        onAfterTriggered = hook
    }
}

