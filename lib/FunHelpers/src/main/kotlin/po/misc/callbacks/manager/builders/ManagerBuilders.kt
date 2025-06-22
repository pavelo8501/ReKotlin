package po.misc.callbacks.manager.builders

import po.misc.callbacks.manager.CallbackManager
import po.misc.callbacks.manager.CallbackManagerHooks
import po.misc.callbacks.manager.CallbackPayload
import po.misc.callbacks.manager.CallbackPayloadBase
import po.misc.callbacks.manager.Containable
import po.misc.callbacks.manager.ResultCallbackPayload
import po.misc.callbacks.manager.models.Configuration
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext

data class SubscriptionBuilder<E: Enum<E>>(
    val manager:  CallbackManager<E>,
    val subscriber: IdentifiableClass
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


inline fun<reified E: Enum<E>> IdentifiableContext.callbackManager(
    vararg payloadsProvider: (CallbackManager<E>) -> CallbackPayloadBase<E, *,* >,
    config: Configuration = Configuration()
): CallbackManager<E>{
    val manager = CallbackManager<E>(E::class.java, this, config)
    payloadsProvider.forEach { payloadProvider->
        payloadProvider(manager)
    }
    return manager
}

inline fun<reified E: Enum<E>> IdentifiableContext.callbackBuilder(
    block: ManagerBuilder<E>.()-> Unit
): CallbackManager<E>{
    val manager = CallbackManager<E>(E::class.java, this)
    val builder = ManagerBuilder(manager)
    block.invoke(builder)
    return  builder.manager
}

inline fun<E: Enum<E>, reified T: Any> ManagerBuilder<E>.createPayload(
    eventType:E,
    noinline block: (PayloadBuilder<E,T>.()-> Unit) ? = null
){
   val payload = CallbackManager.createPayload<E,T>(this.manager, eventType)
    block?.let {
       it.invoke(PayloadBuilder(this.manager, payload))
    }
}

inline fun<E: Enum<E>, reified T: Any, reified R: Any> ManagerBuilder<E>.createPayloadWithResult(
    eventType:E,
    noinline block: (ResultPayloadBuilder<E, T, R>.()-> Unit) ? = null
){

    val payload = CallbackManager.createResultPayload<E, T, R>(this.manager, eventType)
    block?.let {
        it.invoke(ResultPayloadBuilder(this.manager, payload))
    }
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


//firstManager.bridge(secondHolder.dispatcherPayload, firstHolder.routedPayload)

//fun <E2 : Enum<E2>, T: Any> bridge(
//    subscribingPayload: CallbackPayload<E2, T>,
//    requiredPayload:  CallbackPayload<E, T>
//){
//    return requiredPayload.bridge(subscribingPayload)
//}
//
//

fun<E: Enum<E>> IdentifiableClass.withCallbackManager(
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


