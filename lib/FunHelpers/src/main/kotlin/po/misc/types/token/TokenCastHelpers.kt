package po.misc.types.token

import po.misc.data.output.output
import po.misc.debugging.compare
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.debugging.stack_tracer.extractTrace
import po.misc.exceptions.error
import po.misc.types.ClassAware
import po.misc.types.k_class.asClasAware
import po.misc.types.k_class.clasAware
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.safeCast
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf


class CastResult<T> @PublishedApi internal constructor(
    val casted:T?,
    val message: String
){
    val success:Boolean = casted != null
    companion object {
        inline fun <reified T: Any> success(value: T): CastResult<T> = CastResult(value, "Success")
        inline fun <reified T: Any> failure(message: String): CastResult<T> = CastResult(null, message)
    }
}

@PublishedApi
internal fun <T>  KClass<*>.asTAndAny(): KClass<T & Any>{
    @Suppress("UNCHECKED_CAST")
    val kClass = this as? KClass<T & Any>
    if (kClass != null){
        return kClass
    }else{
        error<IllegalArgumentException>("${this.qualifiedName} cannot be cast to <T & Any>", TraceOptions.PreviousMethod)
    }
}

fun <T> Any.safeCast(
    classAware: ClassAware<T>,
):T? =  safeCast<T>(classAware.kClass)


fun <T> Any.castOrThrow(
    classAware: ClassAware<T>
):T {
    return safeCast<T>(classAware.kClass)?:run {
        val msg = "Unable to cast ${this::class.simpleOrAnon} to class  ${classAware.kClass.simpleName}"
        error(msg, TraceOptions.PreviousMethod)
    }
}


inline fun <reified T: Tokenized<TT>, TT> Tokenized<*>.safeCast(
    typeToken: TypeToken<TT>
):T?{
    val casted = safeCast<T>() ?: return null
    return if(casted.typeToken == typeToken){
        casted
    }else{
        null
    }
}


inline fun <reified T: Tokenized<TT>, TT> Tokenized<*>.castOrThrow(
    typeToken: TypeToken<TT>
):T{
  return safeCast<T>()?.let {casted->
        if(casted.typeToken == typeToken){
           casted
        }else{
            val msg = "Provided token $typeToken does not match receivers token ${casted.typeToken}"
            error(msg, TraceOptions.PreviousMethod)
        }
    }?:run {
        val msg = "Unable to cast ${this::class.simpleOrAnon} to class ${T::class.simpleName}"
        error(msg, TraceOptions.PreviousMethod)
    }
}


inline fun <reified T: Any, P> Any.safeTypedCast(
    typeToken: TypeToken<P>
):T? {
    val newToken = TypeToken<T>()
    if(newToken.partialEquality(typeToken)){
       return  safeCast<T>()
    }
    return null
}


/**
 * Attempts to cast this [TokenHolder] to type [T] by validating its contained
 * generic [TypeToken]s.
 *
 * This variant is designed for types that expose multiple generic parameters
 * (e.g. `Holder<T, V>`) and explicitly publish all of them via [TokenHolder.types].
 *
 * The cast succeeds only if:
 * - this instance is assignable to [T], and
 * - at least one of the holder's tokens matches the provided [typeToken]
 *
 * This enables partial generic matching and safe handling of wildcard (`*`)
 * generic positions.
 *
 * @param typeToken the generic token that must be present in the holder
 * @return the casted value if compatible, or `null` otherwise
 */
inline fun <reified T: TokenHolder> TokenHolder.safeCast(
    typeToken: TypeToken<*>
):T?{
    return safeCast<T>()?.let { casted->
        if(casted.types.any { it ==  typeToken}){
            casted
        }else{
            null
        }
    }
}


inline fun <reified T: TokenHolder> TokenHolder.safeCast(
   parameterProvider: ()-> Any?
):T?{
    return safeCast<T>()?.let { casted->
        when(val parameter =  parameterProvider()){
            is TypeToken<*> -> {
               if(casted.typeToken == parameter){
                  return casted
               }
            }
            else-> {
                if(parameter != null){
                    val paramClass = parameter::class
                    val isSubClass = paramClass.isSubclassOf(casted.typeToken.kClass)
                    if(isSubClass) {
                        return casted
                    }
                }else{
                    return null
                }
            }
        }
        return null
    }
}


/**
 * Attempts to cast this [Tokenized] instance to type [T] using an explicit
 * [ClassAware] descriptor for its generic parameter.
 *
 * This function is intended for self-describing generic types where the
 * runtime generic parameter is stored directly on the instance via [Tokenized.typeToken].
 *
 * The cast succeeds only if:
 * - this instance is assignable to [T], and
 * - the stored generic token matches the class described by [classAware]
 *
 * @param classAware runtime description of the expected generic parameter
 * @return the casted value if compatible, or `null` otherwise
 */
inline fun <reified T: Tokenized<P>, P> Tokenized<*>.safeCast(classAware: ClassAware<P>):T?{
    return safeCast<T>()?.let {casted->
        if(casted.typeToken.equals(classAware.kClass)){
            casted
        }else{
            null
        }
    }
}

/**
 * Attempts to safely cast this [Tokenized] instance to type [T] using reified
 * generic parameters.
 *
 * This is a convenience overload of [safeCast] that derives the expected
 * generic parameter type automatically from the reified type [P].
 *
 * This is the most ergonomic and type-safe way to cast tokenized generic objects
 * when both the target type and its parameter are known at the call site.
 *
 * @return the casted value if compatible, or `null` otherwise
 */
inline fun <reified T: Tokenized<P>, reified P> Tokenized<*>.safeCast():T? = safeCast<T, P>(clasAware<P>())


inline fun <reified T: TokenHolder, P> Tokenized<*>.ifCasted(
    typeToken: TypeToken<P>,
    action: T.()->Unit
): CastResult<T> {
    val casted = this.safeCast<T>()
    return casted?.let {
        if (typeToken == casted.typeToken) {
            action.invoke(casted)
            CastResult.success(casted)
        } else {
            CastResult.failure<T>("Token mismatch")
        }
    }?: run {
        CastResult.failure<T>("Base class cast failure")
    }
}


inline fun <reified T: Tokenized<*>> Tokenized<*>.castThrowing(
    token: TypeToken<T>,
):T {
    return try {
        val baseClass = T::class
        val casted = baseClass.cast(this)
        if(casted.typeToken == token){
            casted
        }else{
            error("Check of token ${casted.typeToken} failed for ${baseClass}", TraceOptions.PreviousMethod)
        }
    }catch (th: Throwable){
        th.extractTrace(TraceOptions.PreviousMethod).output()
        throw th
    }
}




