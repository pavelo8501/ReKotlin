package po.misc.types.helpers

import po.misc.data.output.output
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.throwableToText
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.cast



/**
 * Safely attempts to cast this [Tokenized] instance to another [Tokenized] subtype [T],
 * validating both the base type and any inlined type parameters via [po.misc.types.token.TypeToken].
 *
 * This variant supports composite generic validation by checking that all provided
 * [parameterClass] entries match the [inlinedParameters] of the candidate token.
 *
 * @param T The target [Tokenized] type to cast to.
 * @param parameterClass Optional one or more [KClass] values representing the expected
 *        type parameters of [T].
 * @return The successfully casted instance of type [T], or `null` if either:
 *  - The runtime type check fails,
 *  - The underlying [po.misc.types.token.TypeToken] does not match by [partialEquality],
 *
 * @see Tokenized
 * @see po.misc.types.token.TypeToken.partialEquality
 *
 * Example:
 * ```
 * val group: Tokenized<*> = DSLParameterGroup<Test, Int>(...)
 * val casted = group.safeCast<DSLParameterGroup<Test, Int>>(Int::class)
 * if (casted != null) {
 *     casted.applyConfig(...)
 * }
 * ```
 */
inline fun <reified T: Tokenized<T>> Tokenized<*>.safeCast():T? {
    return try {
        val token = TypeToken.create<T>()
        val casted = token.kClass.cast(this)
        return if(casted.typeToken == token){
            casted
        }else{
            null
        }
    }catch (th: Throwable){
        if (th !is ClassCastException) {
            th.extractTrace().output()
            th.throwableToText().output()
        }
        null
    }
}

/**
 * Safely casts this [Tokenized] instance to another parameterized [Tokenized] subtype [T],
 * verifying that the contained [po.misc.types.token.TypeToken] is compatible with the expected parameter [P].
 *
 * @param T The specific [Tokenized] type with parameter type [P].
 * @param P The parameter type expected by [T].
 * @param parameter The [KClass] of the parameter type [P] to validate against.
 * @return The successfully casted instance of type [T], or `null` if the tokenized type
 *         or parameter type mismatch occurs.
 *
 * Example:
 * ```
 * val token: Tokenized<*> = DSLParameterGroup<Test, String>(...)
 * val casted = token.safeCast<DSLParameterGroup<Test, String>, String>(String::class)
 * requireNotNull(casted) { "Token type mismatch" }
 * ```
 */
inline fun <reified T: Tokenized<P>, P: Any> Tokenized<*>.safeCast(
    parameter: KClass<P>,
):T? {
    return try {
        val baseClass = T::class
        val casted = baseClass.cast(this)
        return if(casted.typeToken.partialEquality(parameter)){
            casted
        }else{
            null
        }
    }catch (th: Throwable){
        if (th !is ClassCastException) {
            th.extractTrace().output()
            th.throwableToText().output()
        }
        null
    }
}
