package po.misc.callbacks

import po.misc.callbacks.models.Configuration
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.types.castOrManaged
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import java.util.EnumMap
import kotlin.Any


class CallbackManager<E: Enum<E>>(
    enumClass: Class<E>,
    val emitter: CTX,
    private val config: Configuration = Configuration()
): CTX {

    data class ManagerStats(
        val eventTypesCount: Int,
        val payloadsCount: Int,
        val subscriptionsCount: Int,
        val leftoverDataCount: Int,
        val routedContainersCount: Int
    )

    override val identity: CTXIdentity<CallbackManager<*>>

    init {
        try {
            identity = asSubIdentity(emitter)
        }catch (th: Throwable){
            throw th
        }
    }

   //val identity: Identifiable = identifiable("CallbackManager", emitter.context)

    internal val  singleTypeEventMap = EnumMap<E, MutableList<CallbackPayload<E, *,>>>(enumClass)
    internal val  resultTypeEventMap = EnumMap<E, MutableList<ResultCallbackPayload<E, *, *>>>(enumClass)

    var exceptionOutput: ((ManagedException) -> Unit)? = null
    internal var hooks: CallbackManagerHooks? = null

    private fun <T: Any> containerLookup(
        eventMap:  MutableList<CallbackPayloadBase<E, *, *>>?,
        typedKey: TypeToken<T>
    ): CallbackPayloadBase<E, *, *>?{
        return eventMap?.firstOrNull { it.typeKey == typedKey }
    }

    @PublishedApi
    internal fun <T: Any> registerPayloadInternally(payload: CallbackPayload<E, T> ){
        val found = containerLookup(singleTypeEventMap[payload.eventType]?.safeCast(), payload.typeKey)
        if(found != null){
            throwManaged("Payload with EventType: ${payload.eventType} and key: ${payload.typeKey} already exists. Critical failure", this)
        }else{
            payload.hostingManager = this
            val payloadsForEvent = singleTypeEventMap.getOrPut(payload.eventType) { mutableListOf() }
            payloadsForEvent.add(payload)
        }
    }

    fun <T: Any, R: Any> registerResultPayload(payload: ResultCallbackPayload<E, T, R>){
        val found = containerLookup(resultTypeEventMap[payload.eventType]?.safeCast(), payload.typeKey)
        if(found != null){
            throwManaged("PayloadWithResult EventType: ${payload.eventType} and key: ${payload.typeKey} already exists. Critical failure", this)
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
    internal fun <T: Any> payloadLookup(eventType:E, key:TypeToken<T>):CallbackPayload<E,T>? {
        return singleTypeEventMap[eventType]?.let { event ->
            val payload = event.firstOrNull { it.typeKey == key }
            if (payload != null) {
                payload.castOrManaged<CallbackPayload<E, T>>(this)
            } else {
                val message = "No event registered for : ${key.typeName}"
                hooks?.onFailureHook?.invoke(message)
                if (config.exceptionOnTriggerFailure) {
                    throwManaged(message, this)
                }
                null
            }
        } ?: run {
            val message = "No event registered with name: ${eventType.name}"
            hooks?.onFailureHook?.invoke(message)
            if (config.exceptionOnTriggerFailure) {
                throwManaged(message, this)
            }
            null
        }
    }

//    @PublishedApi
//    internal fun <T: Any, R: Any> resultPayloadLookup(
//        eventType:E,
//        key:ComparableType<T>
//    ): ResultCallbackPayload<E,T,R>?{
//        return resultTypeEventMap[eventType]?.firstOrNull { it.typeKey == key }?.castOrManaged<ResultCallbackPayload<E, T, R>>(this)
//    }

    inline fun <reified T: Any> subscribe(
        subscriber: CTX,
        eventType:E,
        noinline function: (Containable<T>) -> Unit
    ): Unit {
        val key = TypeToken.create<T>()
        payloadLookup(eventType, key)?.subscribe(subscriber, function)?:run {
            throwManaged("Payload for the given eventType: ${eventType.name} and key: $key not registered", this)
        }
        //getPayload<T>(eventType).getOrManaged(exceptionPayload).subscribe(subscriber, function)
    }

    inline fun <reified T: Any> request(
        subscriber: CTX,
        eventType: E,
        noinline function: (Containable<T>) -> Unit
    ): Unit {
        val key = TypeToken.create<T>()
        payloadLookup(eventType, key)?.request(subscriber, function) ?:run {
            throwManaged("Payload for the given eventType: ${eventType.name} and key: $key not registered", this)
        }
    }

    inline fun <reified T: Any> trigger(eventType: E, value: T, subscriber: CTX? = null): Unit
        = trigger(eventType, TypeToken.create<T>(), value, subscriber)

    fun <T: Any> trigger(eventType: E, key: TypeToken<T>, value: T, subscriber: CTX? = null){
        val payload = payloadLookup(eventType, key)
        if(payload != null) {
            subscriber?.let {subscriber->
                payload.trigger(subscriber, value)
            }?:run {
                payload.triggerForAll(value)
            }
        }
    }

//    inline fun <reified T: Any, R: Any> triggerAndExpect(eventType: E, value: T, subscriber: CTX? = null): R
//            =triggerAndExpect(eventType, StaticTypeKey.createTypeKey<T>(), value, subscriber)

//    fun <T: Any, R: Any> triggerAndExpect(eventType: E, key: ComparableType<T>, value: T, subscriber: CTX? = null):R {
//        val payload = resultPayloadLookup<T,R>(eventType, key)
//        if(payload != null) {
//            subscriber?.let { subscriber ->
//                payload.trigger(subscriber, value)
//            }?:run {
//                payload.triggerForAll(value)
//            }
//        }else{
//            throwManaged("Payload for the given eventType: ${eventType.name} and key: $key not registered", this)
//        }
//        TODO("Implement callback result logic")
//    }

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
            val typeKey = TypeToken.create<T>()
            val payload =  CallbackPayload(eventType, typeKey)
            manager.registerPayloadInternally(payload)
            return payload
        }

        fun <E: Enum<E>, T: Any> createPayload(
            manager: CallbackManager<E>,
            eventType:E,
            dataType: TypeToken<T>,
        ): CallbackPayload<E, T>{
            val payload =  CallbackPayload(eventType, dataType)
            manager.registerPayloadInternally(payload)
            return payload
        }

        inline fun <E: Enum<E>, reified T: Any, reified R: Any> createResultPayload(
            manager: CallbackManager<E>,
            eventType:E,
        ): ResultCallbackPayload<E, T, R>{
            val typeKey = TypeToken.create<T>()
            val resultTypeKey = TypeToken.create<R>()
            val payload = ResultCallbackPayload(eventType, typeKey, resultTypeKey)
            manager.registerResultPayload(payload)
            return payload
        }

    }
}