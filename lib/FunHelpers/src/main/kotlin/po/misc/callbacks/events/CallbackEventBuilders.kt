package po.misc.callbacks.events

import po.misc.types.TypeData


/**
 * Creates a standalone event without a host.
 *
 * @param itParamType type information for the event payload.
 * @param builder optional DSL block for registering handlers.
 *
 * Example:
 * ```
 * val click = eventOf<ClickData> {
 *     onEvent { println("Clicked at ${it.x}, ${it.y}") }
 * }
 * ```
 */
fun <T : Any> createEvent(
    itParamType: TypeData<T>,
    builder: (EventBuilder<T, Unit>.() -> Unit)? = null
): CallbackEvent<T, Unit> {
    val event = CallbackEvent<T, Unit>(itParamType)
    builder?.invoke(event)
    return event
}

/**
 * Inline shortcut for [createEvent] using reified payload type.
 */
inline fun <reified T : Any> eventOf(
    noinline builder: (EventBuilder<T, Unit>.() -> Unit)? = null
): CallbackEvent<T, Unit> = createEvent(TypeData.create<T>(), builder)



/**
 * Creates an event bound to a specific [EventHost].
 *
 * @param itParamType type information for the event payload.
 * @param builder optional DSL block for registering handlers.
 *
 * Example:
 * ```
 * class Button : EventHost {
 *     val click = eventOf<ClickData> {
 *         onEvent { println("Button clicked!") }
 *     }
 * }
 * ```
 */
fun <H: EventHost, T: Any> H.createEvent(
    itParamType: TypeData<T>,
    builder: (HostedEventBuilder<H, T, Unit>.() -> Unit)? = null
): ParametrizedEvent<H, T, Unit> {
    val event = ParametrizedEvent<H, T, Unit>(this, itParamType)
    builder?.invoke(event)
    return event
}

/**
 * Inline shortcut for [createEvent] using reified payload type.
 */
inline fun <H: EventHost, reified T: Any> H.eventOf(

    noinline builder: (HostedEventBuilder<H, T, Unit>.() -> Unit)? = null
): ParametrizedEvent<H, T, Unit> = createEvent(TypeData.create<T>(), builder)
