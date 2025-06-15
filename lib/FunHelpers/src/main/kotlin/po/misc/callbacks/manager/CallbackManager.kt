package po.misc.callbacks.manager

import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import po.misc.types.safeCast
import java.util.EnumMap
import kotlin.Any


class CallbackManager<E: Enum<E>>(
    enumClass: Class<E>,
    val emitter: IdentifiableContext,
    vararg val payloads :CallbackPayloadBase<E, *, *>
){
    data class Stats(val eventTypesCount: Int, val subscriptionsCount: Int, val leftoverDataCount: Int, val routedContainersCount: Int)

    private val enumMap = EnumMap<E, CallbackPayloadBase<E, *, *>>(enumClass).apply {
        payloads.forEach {
            it.hostingManager = this@CallbackManager
            put(it.eventType, it)
        }
    }

    internal var hooks : CallbackManagerHooks? = null

    internal fun addPayload(payload: CallbackPayloadBase<E,*,*>){
        enumMap[payload.eventType] = payload
    }


   internal fun <T: Any> payload(eventType:E): CallbackPayload<E, T>{
       return enumMap[eventType].castOrManaged<CallbackPayload<E, T>>()
   }

    fun getStats():Stats{
        var subscriptions: Int = 0
        var leftovers: Int = 0
        var routed: Int = 0
        enumMap.values.forEach{
            event-> event.getStats().also {
                subscriptions += it.subscriptions
                leftovers += it.leftoversCount
                routed += it.routedCount
            }
        }
       return Stats(
           eventTypesCount =  enumMap.size,
           subscriptionsCount = subscriptions,
           leftoverDataCount = leftovers,
           routedContainersCount = routed
       )
    }

    fun <T: Any> getPayload(eventType:E): CallbackPayload<E, T>?{
        return enumMap[eventType]?.safeCast<CallbackPayload<E, T>>()
    }

    fun <T2: Any, T: Any> bridge(
        sourcePayload: CallbackPayload<*, T2>,
        eventType: E,
        dataAdapter: (T2)-> T
    ){
       val payload = enumMap[eventType].castOrManaged<CallbackPayloadBase<E, T, *>>()
       payload.bridge(sourcePayload, dataAdapter)
    }

    fun <T: Any> subscribe(subscriber: IdentifiableClass, eventType:E, function: (Containable<T>)-> Unit){
        getPayload<T>(eventType).getOrManaged(emitter.contextName).subscribe(subscriber, function)
    }
    fun <T: Any> request(subscriber: IdentifiableClass, eventType:E, function: (Containable<T>)-> Unit){
        getPayload<T>(eventType).getOrManaged(emitter.contextName).request(subscriber, function)
    }

    fun <T: Any> trigger(eventType:E, value:T){
        getPayload<T>(eventType).getOrManaged(emitter.contextName).triggerForAll(value)
    }
    fun <T: Any> trigger(subscriber: IdentifiableClass, eventType:E, value:T){
        getPayload<T>(eventType).getOrManaged(emitter.contextName).trigger(subscriber, value)
    }

    fun unsubscribeAll() {
        payloads.forEach { payload ->
            payload.clear()
        }
    }
}