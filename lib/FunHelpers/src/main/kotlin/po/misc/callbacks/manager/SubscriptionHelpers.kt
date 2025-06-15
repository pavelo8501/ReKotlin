package po.misc.callbacks.manager

import po.misc.interfaces.IdentifiableClass


fun<E: Enum<E>, T: Any>  CallbackManager<E>.listen(
    subscriber: IdentifiableClass,
    eventType: E,
    function: (Containable<T>) -> Unit
): CallbackManager<E>  {
    this.subscribe<T>(subscriber, eventType, function)
   return this
}

fun<E: Enum<E>, T: Any>  CallbackManager<E>.requestOnce(
    subscriber: IdentifiableClass,
    eventType: E,
    function: (Containable<T>) -> Unit
): CallbackManager<E>  {
    this.request<T>(subscriber, eventType, function)
    return this
}

fun<E: Enum<E>, T: Any> E.subscribe(init: CallbackPayload<E, T>.() -> Unit): CallbackPayload<E, T> {
    return CallbackPayload<E, T>(this).apply(init)
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
fun <S_EVENT : Enum<S_EVENT>, S_DATA : Any, R_EVENT : Enum<R_EVENT>, R_DATA : Any> CallbackPayload<R_EVENT, R_DATA>.
        bridgeFrom(
    sourcePayloadPayload: CallbackPayload<S_EVENT, S_DATA>?,
    convert: (R_DATA) -> S_DATA
){
    sourcePayloadPayload?.bridge(this, convert)
}



fun <T1: Any, T2: Any> withConverter(converter: (T1) -> T2):(T1) -> T2{
    return converter
}

fun <T : Any> wrapRawCallback(raw: (T) -> Unit): (Containable<T>) -> Unit {
    return { containable -> raw(containable.getData()) }
}
