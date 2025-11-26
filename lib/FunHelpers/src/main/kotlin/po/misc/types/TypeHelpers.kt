package po.misc.types

import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.token.TypeToken
import java.time.LocalDateTime
import kotlin.reflect.KClass


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}

/**
* Ensures that a nullable receiver is not `null`, otherwise throws a custom exception.
*
* This variant allows passing any object as runtime context and optionally an explicit `expectedClass`
* to produce clear diagnostic messages. If the receiver is `null`, an [ExceptionPayload] is constructed
* and passed to [exceptionProvider], which is responsible for instantiating and returning a Throwable.
*
* @param context The object representing execution context (e.g., service, DTO, handler).
*                Used to enrich the exception payload. Prefer implementing [TraceableContext].
* @param expectedClass Optional explicit class expected for the receiver.
*                      If omitted, a readable fallback message is used.
* @param exceptionProvider A factory function that transforms the generated [ExceptionPayload]
*                          into a concrete [Throwable] to be thrown.
*
* @return The non-null receiver value.
*
* @throws Throwable As returned from [exceptionProvider], if the receiver is `null`.
*
* @sample
* val value: String? = null
* value.getOrThrow(this, String::class) { payload ->
    *     IllegalStateException(payload.message)
    * }
*/
fun <T: Any> T?.getOrThrow(
    context: Any,
    expectedClass: KClass<*>? = null,
    exceptionProvider: (ExceptionPayload)-> Throwable,
):T {
    val methodName = "getOrThrow"
    if(this == null){
        val className = expectedClass?.simpleOrAnon?:"ExpectedClass not specified"
        val message = "Expected $className got null."
        val payload = ExceptionPayload(message, methodName, helperMethodName = true,  context)
        throw exceptionProvider.invoke(payload)
    }else{
        return this
    }
}




fun <T: Any> T?.getOrThrow(
    expectedClass: KClass<*>? = null,
):T {
    val methodName = "getOrThrow"
    val className = expectedClass?.simpleOrAnon?:"ExpectedClass not specified"
    val message = "Expected $className got null."
    if(this == null){
        throw NullPointerException(message)
    }else{
        return this
    }
}


fun <T: Any> T?.getOrThrow(
    context: TraceableContext,
    expectedClass: KClass<*>? = null,
):T {
    val methodName = "getOrThrow"
    val className = expectedClass?.simpleOrAnon?:"ExpectedClass not specified"
    val message = "Expected $className got null."
    if(this == null){
        throw NullPointerException(message)
    }else{
        return this
    }
}



/**
 * Reified overload of [getOrThrow] that infers the expected type automatically.
 *
 * Designed for use with traceable contexts, this overload removes boilerplate by:
 * - Inferring `expectedClass` from `T`
 * - Encouraging structured context via [TraceableContext]
 *
 * @param context A strongly-typed execution context implementing [TraceableContext]
 * @param exceptionProvider A factory that converts the generated [ExceptionPayload]
 *                          into a concrete [Throwable] to be thrown.
 *
 * @return The non-null receiver value.
 *
 * @throws Throwable As produced by [exceptionProvider], if receiver is `null`.
 */
inline fun <reified T: Any> T?.getOrThrow(
    context: TraceableContext,
    noinline exceptionProvider: (ExceptionPayload)-> Throwable
):T = getOrThrow(context, T::class, exceptionProvider)


@PublishedApi
@JvmName("getOrManagedImplementation")
internal fun <T> getOrManaged(
    receiver: T?,
    callingContext: Any,
    expectedClass: KClass<*>,
): T{
    val methodName = "getOrManaged"
    if(receiver == null){
        val message = "Expected: ${expectedClass.simpleName}. Result is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw ManagedException(payload)
    }else{
        return receiver
    }
}

fun <T> T?.getOrManaged(
    callingContext: Any,
    expectedClass: KClass<*>,
):T = getOrManaged(this, callingContext,  expectedClass)


inline fun <reified T> T?.getOrManaged(
    callingContext: Any
):T = getOrManaged(this,  callingContext, T::class)


fun Any?.isNull(): Boolean{
    return this == null
}

fun Any?.isNotNull(): Boolean{
    return this != null
}

fun <T: Any> TypeToken<T>.getDefaultForType(): T? {
    val result = when (this.kType.classifier) {
        Int::class -> -1
        String::class -> "Default"
        Boolean::class -> false
        Long::class -> -1L
        LocalDateTime::class -> {
            LocalDateTime.now()
        }
        else -> null
    }
    return result?.safeCast(this.kClass)
}
