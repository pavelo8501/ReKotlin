package po.misc.types

import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.throwableToText
import po.misc.functions.Throwing
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.cast


/**
 * Attempts to cast the receiver to the specified type [T], returning `null` if the cast fails.
 *
 * Unlike [castOrThrow], this method is non-intrusive: it does **not throw** on failure.
 * Instead:
 * - If the cast succeeds, the value is returned as [T].
 * - If a [ClassCastException] occurs, the exception is logged via `throwableToText().output()`
 *   and `null` is returned.
 *
 * This makes `safeCast` ideal for optional or probe-style type checks where
 * exception-based control flow is undesirable.
 *
 * @param kClass The target type to cast to.
 * @return The receiver cast to [T], or `null` if types are incompatible.
 *
 * @sample
 * val unknown: Any = "Hello"
 * val result: Int? = unknown.safeCast(Int::class)  // returns null, logs exception
 */
fun <T: Any> Any.safeCast(
    kClass: KClass<T>,
):T? {
    return try {
        kClass.cast(this)
    }catch (th: Throwable){
        if (th !is ClassCastException) {
            th.extractTrace().output()
            th.throwableToText().output()
        }
        null
    }
}

fun <T> Any.safeCast(token: TypeToken<T>):T? = safeCast(token.kClass)


/**
 * Reified inline variant of [safeCast] that infers the target type [T].
 *
 * This provides a concise and idiomatic Kotlin syntax for safe casting:
 * it returns the receiver as [T] if successful, or `null` if the cast fails.
 * Any [ClassCastException] encountered during casting is logged via `throwableToText().output()`.
 *
 * @return The receiver cast to [T], or `null` if incompatible.
 *
 * @sample
 * val x: Any = "Hello"
 * val number: Int? = x.safeCast<Int>()   // returns null, logs error
 */
inline fun <reified T: Any> Any.safeCast(): T? = safeCast(T::class)


/**
 * Attempts to cast the nullable receiver to the target type [T], or throws a custom exception.
 *
 * This function performs two safety checks:
 * - If the receiver is `null`, delegates to [getOrThrow] to generate a `null`-context exception.
 * - If the receiver is non-null but not of type [kClass], catches [ClassCastException]
 *   and wraps it inside an [ExceptionPayload] for controlled exception construction.
 *
 * @param context The runtime context (service, handler, DTO, etc.) used for payload enrichment.
 *                Prefer instances of [TraceableContext] for structured tracing.
 * @param kClass The expected target type to cast to.
 * @param exceptionProvider A factory that converts [ExceptionPayload] into a [Throwable] to be thrown.
 *
 * @return The successfully casted instance of type [T].
 *
 * @throws Throwable As returned by [exceptionProvider] when:
 *                   - The value is `null`
 *                   - The value cannot be cast to [kClass]
 */
fun <T: Any> Any?.castOrThrow(
    context: Any,
    kClass: KClass<T>,
    exceptionProvider: (ExceptionPayload)-> Throwable,
):T {
    val methodName = "castOrThrow"
    val nullChecked = getOrThrow(context, kClass){payload->
        payload.methodName(methodName, helper = true)
        exceptionProvider(payload)
    }
   return try {
        kClass.cast(nullChecked)
    } catch (th: ClassCastException) {
        val altMsg = "Class can not be cast to ${kClass.simpleOrAnon}"
        val payload = ExceptionPayload(th.message?:altMsg, methodName, true, context)
        payload.cause = th
        throw exceptionProvider(payload)
    }
}


@Throws(ClassCastException::class)
fun <T: Any> Any?.castOrThrow(kClass: KClass<T>):T {
    val methodName = "castOrThrow"
    val nullChecked = getOrThrow(kClass)
    return try {
        kClass.cast(nullChecked)
    } catch (e: ClassCastException) {
        e.extractTrace().output()
        throw e
    }
}


/**
 * Reified overload of [castOrThrow] using an inferred type [T] and traceable context.
 *
 * @param context A structured tracing context implementing [TraceableContext].
 * @param exceptionProvider A factory that converts [ExceptionPayload] into the thrown exception.
 *
 * @return The receiver cast to [T], if valid.
 *
 * @throws Throwable If receiver is `null` or fails `is T` check.
 */
inline fun <reified T: Any> Any?.castOrThrow(
    context: TraceableContext,
    noinline exceptionProvider: (ExceptionPayload)-> Throwable,
): T = castOrThrow(context, T::class, exceptionProvider)


inline fun <reified T: Any> Any?.castOrThrow(): T = castOrThrow(T::class)


fun <T: Any> Any?.castOrManaged(
    callingContext: Any,
    kClass: KClass<T>,
):T {
    val methodName = "castOrManaged"
    var message = "Cast to ${kClass.simpleName} failed."
    if(this == null){
        message += "Source object is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw  ManagedException(payload)
    }else{
        val operation = "Casting ${this::class} to ${kClass.simpleName}"
        return  try {
            kClass.cast(this)
        } catch (e: ClassCastException) {
            val payload = ManagedPayload("$operation ${e.throwableToText()}", methodName, callingContext)
            throw ManagedException(payload.setCause(e))
        }
    }
}

inline fun <reified T: Any> Any?.castOrManaged(callingContext: Any): T  = castOrManaged(callingContext, T::class)

inline fun <reified BASE : Any> Any?.safeBaseCast(): BASE? {
    return when {
        this == null -> null
        BASE::class.java.isAssignableFrom(this::class.java) -> this as BASE
        else -> null
    }
}

@PublishedApi
internal inline fun <reified T : Any, R> runCasted(
    objectToCast: Any,
    crossinline block: T.() -> R
):R? {
    val castedValue = objectToCast.safeCast<T>()
    return if(castedValue != null){
        block.invoke(castedValue)
    }else{
        null
    }
}

inline fun <reified T: Any, R> Any.withCasted(crossinline  block:T.() -> R) : R? = runCasted(this, block)

@Throws(ClassCastException::class)
inline fun <reified T: Any, R> Any.withCasted(throwing: Throwing, crossinline  block:T.() -> R) : R {
   val casted = castOrThrow(T::class)
   return block.invoke(casted)
}