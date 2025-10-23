package po.misc.callbacks

import po.misc.callbacks.components.PayloadAnalyzer
import po.misc.context.CTX
import po.misc.exceptions.throwManaged
import po.misc.types.getOrManaged
import po.misc.types.token.TypeToken


sealed class CallbackPayloadBase<E: Enum<E>, T, R>(
    val eventType: E,
    val typeKey : TypeToken<T>
) where T:Any, R: Any {

    internal val analyzer = PayloadAnalyzer(this)

    internal var hostingManager: CallbackManager<E>? = null

    val manager : CallbackManager<E> by lazy{
        hostingManager.getOrManaged("CallbackManager Init failure")
    }

    abstract val withResult: Boolean

    internal val containers = mutableMapOf<CTX, CallableContainer<T>>()
    internal val routedContainers  = mutableMapOf<CTX, RoutedContainer<T>>()
    internal val leftoverData = mutableListOf<T>()

    internal fun registerManager(manager: CallbackManager<E>){
        hostingManager = manager
    }

    private fun beforeTrigger(container: CallableContainer<T>, value: Any){
        val beforeTriggerEvent = CallbackManagerHooks.TriggerEvent(container.subscriber, eventType, value, manager.emitter)
        manager.hooks?.onBeforeTrigger?.let { it(beforeTriggerEvent) }
    }
    private fun afterTrigger(triggeredCount: Int){
        val postTriggerEvent = CallbackManagerHooks.PostTriggerEvent(triggeredCount, containers.size, manager.emitter)
        manager.hooks?.onAfterTriggered?.let { it(postTriggerEvent) }
    }
    private fun newSubscription(subscriber: CTX){
        val subscriptionEvent =   CallbackManagerHooks.SubscriptionRecord(subscriber, eventType)
        val callbackStats = CallbackManagerHooks.ManagerStats(subscriptionEvent, containers.size, manager.emitter)
        manager.hooks?.onNewSubscription?.let { it(callbackStats) }
    }

    private fun forwarded(container: RoutedContainer<T>){
        manager.hooks?.onForwarding?.let { it(container.routingInfo.last()) }
    }

    private fun removeIfExpired(container: CallableContainer<T>){
        if(container.expires){
            removeContainer(container)
        }
    }
    private fun triggerIfLeftover(subscriber: CTX, function: (Containable<T>)-> Unit){
        leftoverData.forEach {
            val newContainer = CallbackContainer<T>(this, subscriber, 0,  function)
            newContainer.trigger(it)
            leftoverData.remove(it)
        }
    }

    private fun removeContainer(container: CallableContainer<T>){
        if(!containers.values.remove(container)){
            throwManaged("removeContainer fail. No container ${container::class.simpleName.toString()}", this)
        }
    }

    internal fun removeContainer(subscriber: CTX){
        if(containers.containsKey(subscriber)){
            containers.remove(subscriber)
        }else{
            throwManaged("removeContainer fail. No container for key $subscriber", this)
        }
    }

    fun <E2: Enum<E2>> bridge(foreignPayload: CallbackPayload<E2, T>){
        containers.values.forEach {container->
            val createdContainer = foreignPayload.subscribeRouted(container.subscriber,  container.callback)
            createdContainer.addHopInfo(manager.emitter,  container)
        }
    }

    private fun createSubscription(subscriber: CTX, expires: Int,  function: (Containable<T>)-> Unit){
        newSubscription(subscriber)
        val newContainer = CallbackContainer(this, subscriber, expires, function)
        containers[subscriber] = newContainer
        triggerIfLeftover(subscriber, function)
    }
    fun subscribe(subscriber: CTX, function: (Containable<T>)-> Unit) : Unit =
        createSubscription(subscriber, -1, function)
    fun request(subscriber: CTX, function: (Containable<T>)-> Unit) : Unit =
        createSubscription(subscriber, 0, function)

    private fun  createRoutedSubscription(
        subscriber: CTX,
        expires: Int,
       // dataAdapter: (T)->T2,
        function: (Containable<T>)-> Unit) : RoutedContainer<T> {
        val newContainer = RoutedContainer<T>(this, subscriber, expires, function)
        routedContainers[subscriber] = newContainer
        return newContainer
    }


    fun subscribeRouted(subscriber: CTX, function: (Containable<T>)-> Unit)
        = createRoutedSubscription(subscriber, -1,  function)
    fun requestRouted(subscriber: CTX,  function: (Containable<T>)-> Unit)
            = createRoutedSubscription(subscriber, 0,  function)

    fun triggerRouted(value: T, subscriber: CTX? = null): Int{
        var triggersCount = 0
        if(subscriber != null){
            routedContainers.filter { it.key == subscriber }.forEach {(key, routed) ->
                routed.trigger(value)
                forwarded(routed)
                triggersCount++
            }
        }else{
            routedContainers.values.forEach {routed->
                routed.trigger(value)
                forwarded(routed)
                triggersCount++
            }
        }
        return triggersCount
    }

    fun trigger(subscriber: CTX, value: T){
        containers[subscriber]?.let {container->
            beforeTrigger(container, value)
            container.trigger(value)
            removeIfExpired(container)
            val routedCount = triggerRouted(value, subscriber)
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
    fun clear() {
        containers.clear()
        routedContainers.clear()
        leftoverData.clear()
    }
}

class CallbackPayload<E: Enum<E>, T>(
    eventType : E,
    typeKey : TypeToken<T>
): CallbackPayloadBase<E, T, Unit>(eventType, typeKey) where T:Any {
    override val withResult: Boolean = false

    companion object{

       inline fun <E: Enum<E>, reified T: Any> createPayload(
            eventType:E
        ):CallbackPayload<E, T>{
           val typeKey = TypeToken.create<T>()
           return CallbackPayload(eventType, typeKey)
        }

        fun <E: Enum<E>, T: Any> createPayload(
            eventType:E,
            typeKey: TypeToken<T>
        ):CallbackPayload<E, T>{
            return CallbackPayload(eventType, typeKey)
        }
    }
}

class ResultCallbackPayload<E: Enum<E>, T, R>(
    eventType : E,
    typeKey : TypeToken<T>,
    val resultTypeKey:TypeToken<R>
): CallbackPayloadBase<E, T, R>(eventType,typeKey) where T:Any, R:Any {
    override val withResult: Boolean = true
    /**
     * In this context R is T on Callback Manager That estabilish bridge
     */
   // internal val routedContainers2  = mutableMapOf<IdentifiableContext, CallbackPayload<*, *>>()

}
