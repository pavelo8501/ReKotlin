package po.misc.callbacks.manager

import po.misc.exceptions.throwManaged
import po.misc.interfaces.IdentifiableClass
import po.misc.types.getOrManaged


sealed class CallbackPayloadBase<E: Enum<E>, T, R>(
    val eventType: E,
) where T:Any, R: Any {

    data class Stats(val subscriptions: Int, val leftoversCount:Int, val routedCount: Int)

    internal var hostingManager: CallbackManager<E>? = null
    val manager : CallbackManager<E> by lazy{
        hostingManager.getOrManaged("CallbackManager Init failure")
    }

    abstract val withResult: Boolean

    private val containers = mutableMapOf<IdentifiableClass, CallableContainer<T>>()
    private val routedContainers  = mutableMapOf<IdentifiableClass, RoutedContainer<*, T>>()
    private val leftoverData = mutableListOf<T>()

    private fun beforeTrigger(container: CallableContainer<T>, value: Any){
        val beforeTriggerEvent = CallbackManagerHooks.TriggerEvent(container.subscriber, eventType, value, manager.emitter)
        manager.hooks?.onBeforeTrigger?.let { it(beforeTriggerEvent) }
    }
    private fun afterTrigger(triggeredCount: Int){
        val postTriggerEvent = CallbackManagerHooks.PostTriggerEvent(triggeredCount, containers.size, manager.emitter)
        manager.hooks?.onAfterTriggered?.let { it(postTriggerEvent) }
    }
    private fun newSubscription(subscriber: IdentifiableClass){
        val subscriptionEvent =   CallbackManagerHooks.SubscriptionRecord(subscriber, eventType)
        val callbackStats = CallbackManagerHooks.ManagerStats(subscriptionEvent, containers.size, manager.emitter)
        manager.hooks?.onNewSubscription?.let { it(callbackStats) }
    }

    private fun forwarded(container: RoutedContainer<*,*>){
        manager.hooks?.onForwarding?.let { it(container.routingInfo.last()) }
    }

    private fun removeIfExpired(container: CallableContainer<T>){
        if(container.expires){
            removeContainer(container)
        }
    }
    private fun triggerIfLeftover(subscriber: IdentifiableClass, function: (Containable<T>)-> Unit){
        leftoverData.forEach {
            val newContainer = CallbackContainer<T>(this, false, subscriber, function)
            newContainer.trigger(it)
            leftoverData.remove(it)
        }
    }
    private fun triggerRouted(value: T, subscriber: IdentifiableClass? = null): Int{
        var triggersCount = 0
        if(subscriber != null){
            routedContainers.filter { it.key == subscriber }.forEach {(key, routed) ->
                routed.triggerRouted(value)
                forwarded(routed)
                triggersCount++
            }
        }else{
            routedContainers.values.forEach {routed->
                routed.triggerRouted(value)
                forwarded(routed)
                triggersCount++
            }
        }
        return triggersCount
    }
    private fun removeContainer(container: CallableContainer<T>){
        if(!containers.values.remove(container)){
            throwManaged("removeContainer fail. No container ${container::class.simpleName.toString()}")
        }
    }

    internal fun removeContainer(subscriber: IdentifiableClass){
        if(containers.containsKey(subscriber)){
            containers.remove(subscriber)
        }else{
            throwManaged("removeContainer fail. No container for key ${subscriber.completeName}")
        }
    }

    internal fun <T2: Any> subscribeRouted(
        subscriber: IdentifiableClass,
        dataAdapter: (T)->T2,
        function: (Containable<T2>)-> Unit) : RoutedContainer<T2, T> {
        val newContainer = RoutedContainer<T2, T>(this, false, subscriber, dataAdapter,  function)
        routedContainers[subscriber] = newContainer
        return newContainer
    }

    fun <E2: Enum<E2>, T2: Any> bridge(
        payload: CallbackPayloadBase<E2, T2, Unit>,
        dataAdapter: (T2)->T,
    ){
        containers.values.forEach {container->
          val createdContainer = payload.subscribeRouted(container.subscriber, dataAdapter, container.callback)
            createdContainer.addHopInfo(manager.emitter,  container)
        }
    }

    fun <E2: Enum<E2>, T2: Any> bridge(
        subscriber: IdentifiableClass,
        payload: CallbackPayloadBase<E2, T2, Unit>,
        dataAdapter: (T2)->T,
    ): CallableContainer<T>?{
       val foundContainer =   containers.values.firstOrNull  { it.subscriber == subscriber}
       return foundContainer?.let {container->
            val createdContainer = payload.subscribeRouted(container.subscriber, dataAdapter, container.callback)
            createdContainer.addHopInfo(manager.emitter,  container)
        }
    }

    fun subscribe(subscriber: IdentifiableClass, function: (Containable<T>)-> Unit){
        newSubscription(subscriber)
        val newContainer = CallbackContainer(this,  false, subscriber, function)
        containers[subscriber] = newContainer
        triggerIfLeftover(subscriber, function)
    }

    fun request(subscriber: IdentifiableClass, function: (Containable<T>)-> Unit){
        newSubscription(subscriber)
        val newContainer = CallbackContainer(this, true, subscriber, function)
        containers[subscriber] = newContainer
        triggerIfLeftover(subscriber, function)
    }

    fun trigger(subscriber: IdentifiableClass, value: T){
        containers[subscriber]?.let {container->
            beforeTrigger(container, value)
            container.trigger(value)
            removeIfExpired(container)
            val routedCount =  triggerRouted(value, subscriber)
            afterTrigger(routedCount + 1)
        }?:run {
            leftoverData.add(value)
        }
    }
    fun triggerForAll(value: T){
        var triggersCount = 0
        containers.values.forEach {container->
            beforeTrigger(container, value)
            container.trigger(value)
            removeIfExpired(container)
            triggersCount++
        }
        triggersCount += triggerRouted(value)
        if(triggersCount == 0){
            leftoverData.add(value)
        }
        afterTrigger(triggersCount)
    }

    fun getStats(): Stats{
       return Stats(containers.size, leftoverData.size, routedContainers.size)
    }

    fun clear() {
        containers.clear()
        routedContainers.clear()
        leftoverData.clear()
    }
}

class CallbackPayload<E: Enum<E>, T>(
    eventType : E,
): CallbackPayloadBase<E, T, Unit>(eventType) where T:Any {
    override val withResult: Boolean = false
}

class ResultCallbackPayload<E: Enum<E>, T, R>(
    eventType : E,
): CallbackPayloadBase<E, T, R>(eventType) where T:Any, R:Any {
    override val withResult: Boolean = true
}