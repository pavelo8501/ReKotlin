package po.misc.callbacks.signal

import po.misc.context.component.ComponentID
import po.misc.context.component.setName
import po.misc.functions.NoResult
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken

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
fun <T: Any, R: Any> signalOf(
    typeToken: TypeToken<T>,
    resultToken: TypeToken<R>,
    name: String? = null
): Signal<T, R> {
    val signal = Signal<T, R>(typeToken, resultToken)
    if(name != null){
        signal.setName(name)
    }
    return signal
}

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
    name: String? = null
): Signal<T, Unit> {
    val signal = Signal(typeToken, TypeToken.create<Unit>())

    if(name != null){
        signal.setName(name)
    }
    return signal
}

fun <T: Any, R: Any> Tokenized<T>.signalOf(resultToken: TypeToken<R>): Signal<T, R> = signalOf(this.typeToken, resultToken)


fun <T: Any> Tokenized<T>.signalOf(result: NoResult): Signal<T, Unit> = signalOf(this.typeToken, result)

/**
 * Inline shortcut for [signalOf] using a reified payload type.
 */
inline fun <reified T: Any, reified R: Any> signalOf(name: String? = null): Signal<T, R> =
    signalOf(TypeToken.create<T>(), TypeToken.create<R>(), name)

/**
 * Inline shortcut for [signalOf] with no return value (`Unit`).
 */
inline fun <reified T: Any> signalOf(
    result: NoResult,
    name: String? = null
): Signal<T, Unit> = signalOf(TypeToken.create<T>(), result, name)

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
fun <T: Any, R: Any> signal(
    typeToken: TypeToken<T>,
    resultType: TypeToken<R>,
    builder: SignalBuilder<T, R>.() -> Unit
): Signal<T, R> {
    val signal = signalOf<T, R>(typeToken, resultType)
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
    val signal = signalOf(typeToken, result)
    signal.builder()
    return signal
}

/**
 * Inline reified DSL builder for [Signal] with payload [T] and return [R].
 */
inline fun <reified T: Any, reified R: Any> signal(
   noinline builder: SignalBuilder<T, R>.() -> Unit
): Signal<T, R> = signal(TypeToken.create<T>(), TypeToken.create<R>(), builder)

/**
 * Inline reified DSL builder for [Signal] with no return (`Unit`).
 */
inline fun <reified T: Any> signal(
    result: NoResult,
    noinline builder: SignalBuilder<T, Unit>.() -> Unit
): Signal<T, Unit> = signal(TypeToken.create<T>(), result, builder)