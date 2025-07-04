package po.misc.callbacks.manager

import po.misc.callbacks.manager.models.Configuration
import po.misc.collections.StaticTypeKey
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManaged
import po.misc.functions.methodeProbe
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.ObservedContext
import po.misc.types.castOrManaged
import po.misc.types.safeCast
import java.util.EnumMap
import kotlin.Any


class CallbackManager<E: Enum<E>>(
    enumClass: Class<E>,
    val emitter: IdentifiableContext,
    private val config: Configuration = Configuration()
): ObservedContext {

    data class ManagerStats(
        val eventTypesCount: Int,
        val payloadsCount: Int,
        val subscriptionsCount: Int,
        val leftoverDataCount: Int,
        val routedContainersCount: Int
    )

    override val contextName: String = "CallbackManager"
    override val sourceName: String = emitter.contextName

    internal val  singleTypeEventMap = EnumMap<E, MutableList<CallbackPayload<E, *,>>>(enumClass)
    internal val  resultTypeEventMap = EnumMap<E, MutableList<ResultCallbackPayload<E, *, *>>>(enumClass)

    override var exceptionOutput: ((ManagedException) -> Unit)? = null
    internal var hooks: CallbackManagerHooks? = null

    private fun <T: Any> containerLookup(
        eventMap:  MutableList<CallbackPayloadBase<E, *, *>>?,
        typedKey: StaticTypeKey<T>
    ): CallbackPayloadBase<E, *, *>?{
        return eventMap?.firstOrNull { it.typeKey == typedKey }
    }

    fun <T: Any> registerPayload(payload: CallbackPayload<E, T> ){
        val found = containerLookup(singleTypeEventMap[payload.eventType]?.safeCast(), payload.typeKey)
        if(found != null){
            throwManaged("Payload with EventType: ${payload.eventType} and key: ${payload.typeKey} already exists. Critical failure")
        }else{
            payload.hostingManager = this
            val payloadsForEvent = singleTypeEventMap.getOrPut(payload.eventType) { mutableListOf() }
            payloadsForEvent.add(payload)
        }
    }

    fun <T: Any, R: Any> registerResultPayload(payload: ResultCallbackPayload<E, T, R>){
        val found = containerLookup(resultTypeEventMap[payload.eventType]?.safeCast(), payload.typeKey)
        if(found != null){
            throwManaged("PayloadWithResult EventType: ${payload.eventType} and key: ${payload.typeKey} already exists. Critical failure")
        }else{
            payload.hostingManager = this
            val payloadsForEvent = resultTypeEventMap.getOrPut(payload.eventType) { mutableListOf() }
            payloadsForEvent.add(payload)
        }
    }

    fun getStats(): ManagerStats {
        var subscriptions: Int = 0
        var leftovers: Int = 0
        var routed: Int = 0
        var payloads: Int =0
        singleTypeEventMap.values.flatMap { it }.forEach {payload->
            payload.analyzer.getStats().let { record ->
                payloads++
                subscriptions += record.subscriptionCount
                leftovers += record.leftoverDataCount
                routed += record.routedContainersCount
            }
        }
        resultTypeEventMap.values.flatMap { it }.forEach {payload->
            payload.analyzer.getStats().let { record ->
                payloads++
                subscriptions += record.subscriptionCount
                leftovers += record.leftoverDataCount
                routed += record.routedContainersCount
            }
        }
        return ManagerStats(
            eventTypesCount = singleTypeEventMap.size,
            subscriptionsCount = subscriptions,
            payloadsCount = payloads,
            leftoverDataCount = leftovers,
            routedContainersCount = routed
        )
    }

    fun setExceptionHandler(handler: (ManagedException) -> Unit) {
        exceptionOutput = handler
    }

    @PublishedApi
    internal fun <T: Any> payloadLookup(eventType:E, key:StaticTypeKey<T>):CallbackPayload<E,T>? {
        return singleTypeEventMap[eventType]?.let { event ->
            val payload = event.firstOrNull { it.typeKey == key }
            if (payload != null) {
                payload.castOrManaged<CallbackPayload<E, T>>()
            } else {
                val message = "No event registered for : ${key.typeName}"
                hooks?.onFailureHook?.invoke(message)
                if (config.exceptionOnTriggerFailure) {
                    throwManaged(message)
                }
                null
            }
        } ?: run {
            val message = "No event registered with name: ${eventType.name}"
            hooks?.onFailureHook?.invoke(message)
            if (config.exceptionOnTriggerFailure) {
                throwManaged(message)
            }
            null
        }
    }

    @PublishedApi
    internal fun <T: Any, R: Any> resultPayloadLookup(
        eventType:E,
        key:StaticTypeKey<T>
    ): ResultCallbackPayload<E,T,R>?{
        return resultTypeEventMap[eventType]?.firstOrNull { it.typeKey == key }?.castOrManaged<ResultCallbackPayload<E, T, R>>()
    }

    inline fun <reified T: Any> subscribe(
        subscriber: IdentifiableClass,
        eventType:E,
        noinline function: (Containable<T>) -> Unit
    ): Unit = methodeProbe("subscribe", subscriber) {
        val key = StaticTypeKey.createTypeKey<T>()
        payloadLookup(eventType, key)?.subscribe(subscriber, function)?:run {
            throwManaged("Payload for the given eventType: ${eventType.name} and key: $key not registered")
        }
        //getPayload<T>(eventType).getOrManaged(exceptionPayload).subscribe(subscriber, function)
    }

    inline fun <reified T: Any> request(
        subscriber: IdentifiableClass,
        eventType: E,
        noinline function: (Containable<T>) -> Unit
    ): Unit = methodeProbe("request", eventType) {
        val key = StaticTypeKey.createTypeKey<T>()
        payloadLookup(eventType, key)?.request(subscriber, function) ?:run {
            throwManaged("Payload for the given eventType: ${eventType.name} and key: $key not registered")
        }
    }

    inline fun <reified T: Any> trigger(eventType: E, value: T, subscriber: IdentifiableClass? = null): Unit
        = trigger(eventType, StaticTypeKey.createTypeKey<T>(), value, subscriber)

    fun <T: Any> trigger(eventType: E, key: StaticTypeKey<T>, value: T, subscriber: IdentifiableClass? = null){
        val payload = payloadLookup(eventType, key)
        if(payload != null) {
            subscriber?.let {subscriber->
                payload.trigger(subscriber, value)
            }?:run {
                payload.triggerForAll(value)
            }
        }
    }

    inline fun <reified T: Any, R: Any> triggerAndExpect(eventType: E, value: T, subscriber: IdentifiableClass? = null): R
            =triggerAndExpect(eventType, StaticTypeKey.createTypeKey<T>(), value, subscriber)

    fun <T: Any, R: Any> triggerAndExpect(eventType: E, key: StaticTypeKey<T>, value: T, subscriber: IdentifiableClass? = null):R {
        val payload = resultPayloadLookup<T,R>(eventType, key)
        if(payload != null) {
            subscriber?.let { subscriber ->
                payload.trigger(subscriber, value)
            }?:run {
                payload.triggerForAll(value)
            }
        }else{
            throwManaged("Payload for the given eventType: ${eventType.name} and key: $key not registered")
        }
        TODO("Implement callback result logic")
    }

    fun <E2 : Enum<E2>, T: Any> bridge(
        subscribingPayload: CallbackPayload<E2, T>,
        requiredPayload:  CallbackPayload<E, T>
    ){
       return requiredPayload.bridge(subscribingPayload)
    }

    fun unsubscribeAll() {
        singleTypeEventMap.values.forEach { it.clear() }
        resultTypeEventMap.values.forEach { it.clear() }
    }

    companion object{

        inline fun <E: Enum<E>, reified T: Any> createPayload(
            manager: CallbackManager<E>,
            eventType:E,
        ): CallbackPayload<E, T>{
            val typeKey = StaticTypeKey.createTypeKey<T>()
            val payload =  CallbackPayload(eventType, typeKey)
            manager.registerPayload(payload)
            return payload
        }

        inline fun <E: Enum<E>, reified T: Any, reified R: Any> createResultPayload(
            manager: CallbackManager<E>,
            eventType:E,
        ): ResultCallbackPayload<E, T, R>{
            val typeKey = StaticTypeKey.createTypeKey<T>()
            val resultTypeKey = StaticTypeKey.createTypeKey<R>()

            val payload = ResultCallbackPayload(eventType, typeKey, resultTypeKey)
            manager.registerResultPayload(payload)
            return payload
        }

    }
}