package po.misc.callbacks.signal

import po.misc.context.component.setName
import po.misc.functions.NoResult
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken

@PublishedApi
internal fun <T: Any> createSignal(
    typeToken: TypeToken<T>,
    options: SignalOptions? = null
): Signal<T, Unit> {
    return Signal(typeToken, TypeToken.create<Unit>(), options)
}

@PublishedApi
internal fun <T: Any, R> createSignal(
    typeToken: TypeToken<T>,
    resultToken: TypeToken<R>,
    options: SignalOptions? = null
): Signal<T, R> {
    return Signal(typeToken, resultToken, options)
}

/**
 * Creates a standalone [Signal] without an owner (unbound event).
 *
 * This factory is used when you need an event-like callback channel
 * that is not tied to any [po.misc.callbacks.events.EventHost] or component instance.
 *
 * @param typeToken type information for the input payload [T].
 * @return a new [Signal] instance that can register listeners and dispatch payloads.
 *
 * @see signal for the DSL builder variant with configuration block.
 */
fun <T: Any, R> signalOf(
    typeToken: TypeToken<T>,
    resultToken: TypeToken<R>,
    options: SignalOptions? = null
): Signal<T, R> = createSignal(typeToken, resultToken, options)

/**
 * Variant of [signalOf] for signals that do not return a result (`Unit` response).
 *
 * Passing [NoResult] explicitly resolves overload ambiguity when the signal
 * carries only a payload type [T] and no return value.
 *
 * @param typeToken type information for the input payload [T].
 */
fun <T: Any> signalOf(
    typeToken: TypeToken<T>,
    result: NoResult,
    options: SignalOptions? = null
): Signal<T, Unit> = createSignal(typeToken, options)

fun <T: Any, R: Any> Tokenized<T>.signalOf(
    resultToken: TypeToken<R>
): Signal<T, R> = createSignal(typeToken, resultToken)


fun <T: Any> Tokenized<T>.signalOf(
    result: NoResult
): Signal<T, Unit>{

  return  createSignal(typeToken)

}

/**
 * Inline shortcut for [signalOf] using a reified payload type.
 */
inline fun <reified T: Any, reified R> signalOf(
    options: SignalOptions? = null
): Signal<T, R> = createSignal(TypeToken.create<T>(), TypeToken.create<R>(), options)

/**
 * Inline shortcut for [signalOf] with no return value (`Unit`).
 */
inline fun <reified T: Any> signalOf(
    result: NoResult,
    options: SignalOptions? = null
): Signal<T, Unit> = createSignal(TypeToken.create<T>(), options)

/**
 * Creates and configures a [Signal] using a builder block.
 *
 * This is the preferred DSL entry point for defining listeners directly.
 *
 * Example:
 * ```
 * val onTick = signal<Tick> {
 *     on { println("Tick received: $it") }
 * }
 * ```
 *
 * @param typeToken type information for the input payload [T].
 * @param builder configuration block for registering listeners via [EventBuilder].
 */
fun <T: Any, R> signal(
    typeToken: TypeToken<T>,
    resultType: TypeToken<R>,
    builder: SignalBuilder<T, R>.() -> Unit
): Signal<T, R> {
    val signal = createSignal(typeToken, resultType)
    signal.builder()
    return signal
}

/**
 * DSL builder for a [Signal] that does not return a result (`Unit`).
 *
 * Use when emitting only events without collecting results from listeners.
 *
 * @param result marker [NoResult] indicating `Unit` return type.
 * @param builder configuration block for this signal.
 */
fun <T: Any> signal(
    typeToken: TypeToken<T>,
    result: NoResult,
    builder: SignalBuilder<T, Unit>.() -> Unit
): Signal<T, Unit> {
    val signal = createSignal(typeToken)
    signal.builder()
    return signal
}

/**
 * Inline reified DSL builder for [Signal] with payload [T] and return [R].
 */
inline fun <reified T: Any, reified R> signal(
   noinline builder: SignalBuilder<T, R>.() -> Unit
): Signal<T, R> = signal(TypeToken.create<T>(), TypeToken.create<R>(), builder)

/**
 * Inline reified DSL builder for [Signal] with no return (`Unit`).
 */
inline fun <reified T: Any> signal(
    result: NoResult,
    noinline builder: SignalBuilder<T, Unit>.() -> Unit
): Signal<T, Unit> = signal(TypeToken.create<T>(), result, builder)