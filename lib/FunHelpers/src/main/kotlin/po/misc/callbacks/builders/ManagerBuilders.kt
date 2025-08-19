package po.misc.callbacks.builders

import po.misc.callbacks.CallbackManager
import po.misc.callbacks.CallbackManagerHooks
import po.misc.callbacks.CallbackPayload
import po.misc.callbacks.CallbackPayloadBase
import po.misc.callbacks.Containable
import po.misc.callbacks.ResultCallbackPayload
import po.misc.callbacks.models.Configuration
import po.misc.context.CTX
import po.misc.functions.containers.DeferredContainer
import po.misc.context.Identifiable

data class SubscriptionBuilder<E: Enum<E>>(
    val manager:  CallbackManager<E>,
    val subscriber: CTX
)

data class ManagerBuilder<E: Enum<E>>(
    val manager:  CallbackManager<E>,
)

data class PayloadBuilder<E: Enum<E>, T: Any>(
    val manager:  CallbackManager<E>,
    val payload: CallbackPayload<E, T>,
)

data class ResultPayloadBuilder<E: Enum<E>, T: Any, R: Any>(
    val manager:  CallbackManager<E>,
    val payload: ResultCallbackPayload<E, T, R>,
)


inline fun<reified E: Enum<E>> CTX.callbackManager(
    vararg payloadsProvider: CallbackManager<E>.() -> CallbackPayloadBase<E, *,* >,
    config: Configuration = Configuration()
): CallbackManager<E>{
    val manager = CallbackManager<E>(E::class.java, this, config)
    payloadsProvider.forEach { payloadProvider->
        payloadProvider(manager)
    }
    return manager
}



//inline fun<T:IdentifiableContext, reified E: Enum<E>> T.callbackBuilder2(
//   noinline block: CallbackManager<E>.()-> Unit
//): T{
//
//    val manager = CallbackManager<E>(E::class.java, this)
//    val builder = ManagerBuilder(manager)
//    block.invoke(manager)
//    return this
//}

inline fun<reified E: Enum<E>> CTX.callbackBuilder(
    block: CallbackManager<E>.()-> Unit
): CallbackManager<E>{
    val manager = CallbackManager<E>(E::class.java, this)
    val builder = ManagerBuilder(manager)
    block.invoke(manager)
    return manager
}

fun<E: Enum<E>, T: Any> CallbackManager<E>.registerPayload(
    payload: CallbackPayload<E, T>,
    block: (PayloadBuilder<E, T>.()-> Unit) ? = null
): CallbackPayload<E, T>{
    payload.registerManager(this)
    val payloadBuilder =  PayloadBuilder(this, payload)
    block?.invoke(payloadBuilder)
    return payload
}

inline fun<E: Enum<E>, reified T: Any> CallbackManager<E>.createPayload(
    eventType:E,
    noinline block: (PayloadBuilder<E, T>.()-> Unit) ? = null
): CallbackPayload<E, T>{
    val payload = CallbackManager.createPayload<E, T>(this, eventType)
    val payloadBuilder =  PayloadBuilder(this, payload)
    block?.invoke(payloadBuilder)
    return payload
}

inline fun<E: Enum<E>, reified T: Any> CallbackManager<E>.createPayloadDeferred(
    eventType:E,
    noinline block: (PayloadBuilder<E, T>.()-> Unit) ? = null
): DeferredContainer<CallbackPayload<E, T>>{
    val payload = CallbackManager.createPayload<E, T>(this, eventType)
    val payloadBuilder =  PayloadBuilder(this, payload)
    block?.invoke(payloadBuilder)
   return DeferredContainer<CallbackPayload<E, T>>(this){
        payload
    }
}

inline fun<E: Enum<E>, reified T: Any, reified R: Any> CallbackManager<E>.createPayloadWithResult(
    eventType:E,
    noinline block: (ResultPayloadBuilder<E, T, R>.()-> Unit) ? = null
):ResultPayloadBuilder<E, T, R>{

    val payload = CallbackManager.createResultPayload<E, T, R>(this, eventType)
    val payloadBuilder = ResultPayloadBuilder(this, payload)
    block?.invoke(payloadBuilder)
  return  payloadBuilder
}


/**
 * Establishes a routed bridge from the given [sourcePayloadPayload] to this payload.
 *
 * This operation effectively **adopts active subscriptions** from the source payload and
 * **redirects their data** (of type [S_DATA]) into this payload, after applying the provided [convert] function
 * which transforms data from [R_DATA] to [S_DATA].
 *
 * This method is intended to be called **on the destination payload** (`this`), while the actual routing
 * setup and data forwarding is performed inside the source payload via its `bridge(...)` method.
 *
 * ---
 *
 * ### Example usage:
 * ```
 * destinationPayload.bridgeFrom(sourcePayload) { sourceData ->
 *     convertToDestinationFormat(sourceData)
 * }
 * ```
 *
 * ### Flow:
 * - The [sourcePayloadPayload] holds active containers and subscribers
 * - This method registers a bridge in the source payload which,
 *   when triggered, will:
 *     1. Convert its own emitted data using [convert]
 *     2. Forward the result into this payload’s callbacks
 *
 * ---
 *
 * @param sourcePayloadPayload The originating payload from which callbacks are routed
 * @param convert A conversion function that maps [R_DATA] (destination type) to [S_DATA] (source type),
 *                used to transform routed data before forwarding
 */
fun <E: Enum<E>, T: Any> PayloadBuilder<E, T>.bridgeFrom(
    sourcePayloadPayload: CallbackPayload<*, T>
){
    sourcePayloadPayload.bridge(this.payload)
}

fun<E: Enum<E>> CTX.withCallbackManager(
    manager: CallbackManager<E>,
    block : SubscriptionBuilder<E>.() -> Unit
):CallbackManager<E>{
    val builder = SubscriptionBuilder(manager, this)
    block.invoke(builder)
    return builder.manager
}

inline fun<E: Enum<E>, reified T: Any> SubscriptionBuilder<E>.listen(
    eventType: E,
    noinline function: (Containable<T>) -> Unit
):SubscriptionBuilder<E>{
    val subscriber = this.subscriber
    this.manager.subscribe(subscriber, eventType, function)
    return this
}

inline fun<E: Enum<E>, reified T: Any>  SubscriptionBuilder<E>.requestOnce(
    eventType: E,
    noinline function: (Containable<T>) -> Unit
): SubscriptionBuilder<E>  {
    val subscriber = this.subscriber
    this.manager.request(subscriber, eventType, function)
    return this
}

fun <T: CallbackManager<*>> T.managerHooks(hooks: CallbackManagerHooks.()-> Unit){
    val hooksInstance = CallbackManagerHooks().apply(hooks)
    this.hooks = hooksInstance
}


