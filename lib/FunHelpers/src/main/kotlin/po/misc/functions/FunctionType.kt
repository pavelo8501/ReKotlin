package po.misc.functions

import po.misc.types.token.TypeToken
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * Marker type used to indicate that a signal listener expects a suspending
 * callback function.
 *
 * This is used in DSL overloads such as:
 * `onSignal(LambdaType.Suspended) { suspend (T) -> R }`
 *
 * Its sole purpose is to disambiguate overloads between regular and
 * suspending listeners.
 */
sealed interface LambdaType

object Sync: LambdaType
object Suspended: LambdaType


/**
 * Base type for all signal listener configuration options.
 *
 * Implementations represent how the listener should behave after being
 * invoked (e.g., persist or auto-remove).
 *
 * This interface allows the signal system to remain generic while still
 * supporting different option types for synchronous and suspending listeners.
 */
sealed interface CallableOptions

/**
 * Options for **suspending** signal listeners.
 *
 * These objects serve two roles:
 * 1. They define listener behavior (`Listen` vs `Promise`).
 * 2. They disambiguate overloads so that suspending callbacks select the
 *    correct API method without compiler ambiguity.
 */
sealed interface SuspendedOptions: CallableOptions{

    val name: String?

    /**
     * The listener remains registered after being invoked.
     */
    object Listen : SuspendedOptions{
        override var name: String? = null
    }

    /**
     * The listener is automatically removed after the first successful
     * invocation. Useful for "single-shot" coroutine callbacks.
     */
    object Promise : SuspendedOptions{
        override var name: String? = null
    }
}

/**
 * Options for **non-suspending** (regular) signal listeners.
 *
 * These objects mirror [SuspendedOptions] but exist as a separate sealed type
 * to ensure Kotlin can correctly resolve overloaded API calls.
 *
 * Without this type separation, onSignal overloads with lambda and suspend
 * lambda callbacks would become ambiguous.
 */
sealed interface LambdaOptions: CallableOptions{

    /**
     * The listener remains registered after being invoked.
     */
    object Listen : LambdaOptions
    object Promise : LambdaOptions

    open class NamedListen(var name: String) : LambdaOptions

    /**
     * The listener is automatically removed after the first successful
     * invocation. Useful for "single-shot" coroutine callbacks.
     */
    open class NamedPromise(var name: String) : LambdaOptions

}

/**
 * Marker for functions where an exception is considered part of the
 * expected control flow. Mainly useful in generic wrapping utilities.
 */
sealed interface FunctionType

/**
 * Marker for functions where an exception is considered part of the
 * expected control flow. Mainly useful in generic wrapping utilities.
 */
object Throwing: FunctionType

/**
 * Describes the expected nullability of a function result.
 *
 * This abstraction allows generic wrappers to reason about return types
 * at runtime without relying on reified type parameters alone.
 */
sealed interface FunctionResultType

/**
 * Indicates that the function result cannot be null.
 */
object NonNullable: FunctionResultType

/**
 * Indicates that the function may return null.
 */
object Nullable: FunctionResultType

/**
 * Indicates that a function returns `Unit`.
 *
 * Used in DSL overloads where `Unit` should be treated as a separate
 * semantic result category, distinct from nullable or non-nullable values.
 */
object NoResult: FunctionResultType{
    val token : TypeToken<Unit> = TypeToken<Unit>()
}

/**
 * Indicates that a function returns `Nothing`.
 *
 * This is used to mark APIs where the callback never returns normally
 * (e.g., it always throws or suspends forever).
 */
object NoReturn: FunctionResultType

interface FunctionKind
object NoParam: FunctionKind{
    val token : TypeToken<Unit> = TypeToken<Unit>()
}


/**
 * Represents the kind of Kotlin property being resolved using reflection.
 *
 * `ReadOnlyProperty` corresponds to [KProperty1],
 * `MutableProperty` corresponds to [KMutableProperty1].
 */
sealed interface PropertyKind : CallableOptions

object Readonly: PropertyKind
object Mutable  :PropertyKind
