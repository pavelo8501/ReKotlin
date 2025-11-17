package po.misc.callbacks.event

import po.misc.callbacks.common.EventHost
import po.misc.callbacks.signal.Signal
import po.misc.context.component.setName
import po.misc.functions.NoResult
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken



@PublishedApi
internal fun <H: EventHost, T: Any, R> createEvent(
    host: H,
    typeToken: TypeToken<T>,
    resultType: TypeToken<R>,
    name: String? = null
): HostedEvent<H, T, R> {
    val event = HostedEvent(host,  typeToken, resultType)
    if(name != null){
        event.setName(name)
    }
    return event
}


@PublishedApi
internal fun <H: EventHost, T: Any> createEvent(
    host: H,
    typeToken: TypeToken<T>,
    name: String? = null
): HostedEvent<H, T, Unit> {
    val event = HostedEvent(host,  typeToken, TypeToken.create<Unit>())
    if(name != null){
        event.setName(name)
    }
    return event
}

/**
 * Creates a hosted [po.misc.callbacks.events.HostedEvent] bound to this [po.misc.callbacks.events.EventHost].
 *
 * Unlike standalone [Signal] dispatchers, hosted events are associated with
 * a specific owner component and represent domain-level events such as
 * `Button.click` or `Form.submit`.
 *
 * @param parameterType type information for the payload [T].
 * @param resultType type information for the return type [R].
 * @return a new [po.misc.callbacks.events.HostedEvent] owned by this [po.misc.callbacks.events.EventHost].
 *
 * @see event for the builder-based variant with handler registration.
 */
fun <H: EventHost, T: Any, R> H.eventOf(
    parameterType: TypeToken<T>,
    resultType: TypeToken<R>,
    name: String? = null
): HostedEvent<H, T, R> = createEvent(this, parameterType, resultType, name)

/**
 * Variant of [eventOf] for events that do not return a result (`Unit`).
 *
 * Passing [NoResult] explicitly enforces `Unit` as the return type and
 * resolves overload ambiguity.
 *
 * @param parameterType type information for the payload [T].
 */
fun <H: EventHost, T: Any> H.eventOf(
    parameterType: TypeToken<T>,
    result: NoResult,
    name: String? = null
): HostedEvent<H, T, Unit> = createEvent(this, parameterType, name)

fun <H: EventHost, T: Any> H.eventOf(
    parameter: Tokenized<T>,
    result: NoResult, name: String? = null
): HostedEvent<H, T, Unit> = createEvent(this, parameter.typeToken, name)


fun <H: EventHost, T: Any, R> H.eventOf(
    parameter: Tokenized<T>,
    result: Tokenized<R>,
    name: String? = null
): HostedEvent<H, T, R> = createEvent(this,  parameter.typeToken, result.typeToken, name)

/**
 * Inline shortcut for [eventOf] using reified types for payload and result.
 */
inline fun <H: EventHost, reified T: Any, reified R> H.eventOf(
    name: String? = null
): HostedEvent<H, T, R> = createEvent(this, TypeToken.create<T>(), TypeToken.create<R>(), name)

/**
 * Inline shortcut for [eventOf] using reified payload type and no result (`Unit`).
 */
inline fun <H: EventHost, reified T: Any> H.eventOf(
    result: NoResult,
    name: String? = null
): HostedEvent<H, T, Unit> = createEvent(this,  TypeToken.create<T>(), name)


/**
 * Creates and configures a [HostedEvent] using a builder DSL.
 *
 * Preferred form for domain event declarations on components:
 *
 * ```
 * class Button : EventHost {
 *     val click = event<ClickData> {
 *         onEvent { println("Button clicked: $it") }
 *     }
 * }
 * ```
 *
 * @param parameterType type information for the payload [T].
 * @param resultType type information for the return type [R].
 * @param builder DSL block to configure event handlers.
 */
fun <H: EventHost, T: Any, R> H.event(
    parameterType: TypeToken<T>,
    resultType: TypeToken<R>,
    builder: HostedEventBuilder<H, T, R>.() -> Unit
): HostedEvent<H, T, R> {
    val event = createEvent(this, parameterType, resultType)
    event.builder()
    return event
}

/**
 * DSL builder for hosted events without a return result (`Unit`).
 *
 * @param parameterType type information for the payload [T].
 * @param result marker [NoResult] enforcing `Unit`.
 * @param builder DSL block to configure event handlers.
 */
fun <H: EventHost, T: Any> H.event(
    parameterType: TypeToken<T>,
    result: NoResult,
    builder: HostedEventBuilder<H, T, Unit>.() -> Unit
): HostedEvent<H, T, Unit> {
    val event = createEvent(this, parameterType)
    event.builder()
    return event
}

/**
 * Inline reified DSL builder for hosted events with payload [T] and result [R].
 */
inline fun <H: EventHost, reified T: Any, reified R> H.event(
    noinline builder: HostedEventBuilder<H, T, R>.() -> Unit
): HostedEvent<H, T, R> = event(TypeToken.create<T>(), TypeToken.create<R>(), builder)

/**
 * Inline reified DSL builder for hosted events with no return result (`Unit`).
 */
inline fun <H: EventHost, reified T: Any> H.event(
    result: NoResult,
    noinline builder: HostedEventBuilder<H, T, Unit>.() -> Unit
): HostedEvent<H, T, Unit> = event(TypeToken.create<T>(), result, builder)
