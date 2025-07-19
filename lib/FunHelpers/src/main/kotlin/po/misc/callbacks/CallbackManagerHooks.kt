package po.misc.callbacks

import po.misc.context.CTX
import po.misc.context.Identifiable


class CallbackManagerHooks{

    data class SubscriptionRecord(val subscriber: CTX, val eventType: Enum<*>)
    data class ManagerStats(val newEntry: SubscriptionRecord, val registrySize: Int, val emitter: CTX)
    data class TriggerEvent(val subscriber: CTX, val eventType: Enum<*>, val value: Any, val emitter: CTX)
    data class PostTriggerEvent(val triggeredCount: Int, val registrySize: Int, val emitter: CTX)

    internal var onNewSubscription: ((ManagerStats)-> Unit)? = null
    internal var onBeforeTrigger: ((TriggerEvent)-> Unit)? = null
    internal var onAfterTriggered: ((PostTriggerEvent)-> Unit)? = null
    internal var onFailureHook: ((String)-> Unit)? = null

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

    fun onFailure(hook: (String)-> Unit) {
        onFailureHook = hook
    }

}

