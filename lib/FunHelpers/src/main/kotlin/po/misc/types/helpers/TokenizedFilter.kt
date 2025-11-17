package po.misc.types.helpers

import po.misc.types.safeCast
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import kotlin.collections.filterIsInstance
import kotlin.reflect.KClass


/**
 * Performs a strongly typed filtration of an arbitrary [collection],
 * matching only elements of type [expectedClass] (or its subclasses)
 * and—if provided—verifying generic type parameters against runtime [TypeToken] data.
 *
 * This function is primarily used by higher-level reified filters such as [filterByType] and [filterByTypeWhere].
 * It maintains an internal [Filtration] record describing each filtering stage, enabling verbose diagnostics
 * of runtime and generic parameter resolution.
 *
 * ### Behavior overview
 * 1. **Runtime filtering:**
 *    Retains only elements that can be safely cast to [expectedClass].
 *    Optionally applies an additional [predicate] to further restrict results.
 *
 * 2. **Type parameter filtering:**
 *    If [typeParameters] are provided and the candidate implements [Tokenized],
 *    its [Tokenized.typeToken] is compared against the provided parameters.
 *    The check is deterministic (sorted by simple name) and fails if parameters differ.
 *
 * 3. **Diagnostics:**
 *    Every filtering phase is registered in a [Filtration] log object.
 *    If the final result is empty, a detailed trace of operations is emitted via [Filtration.output].
 *
 * @param collection the source list (can contain mixed or null elements).
 * @param expectedClass the base KClass used to perform safe casting.
 * @param typeParameters optional list of expected type parameters (e.g. `[ComponentInt::class, SealedBase::class]`).
 * @param predicate optional additional condition applied to each successfully casted element.
 * @return a list of elements of type [T] that match the runtime type and (if applicable) generic parameter conditions.
 *
 * @see filterByType
 * @see filterByTypeWhere
 */
@PublishedApi
internal fun <T: Tokenized<*>> tokenizedFiltrator(
    collection: List<Tokenized<*>>,
    baseClass: KClass<T>,
    typeToken: TypeToken<*>
): List<T> {

    val filtered = collection.filterIsInstance<Tokenized<T>>()

    val typeFiltered = filtered.mapNotNull {tokenHolder->
        if(tokenHolder.typeToken.strictEquality(typeToken)){
            tokenHolder.safeCast(baseClass)
        }else{
            null
        }
    }
    return typeFiltered
}


inline fun <reified T: Tokenized<*>> List<Tokenized<*>>.filterTokenized(
    typeToken: TypeToken<*>
): List<T>{
   return tokenizedFiltrator(this, T::class, typeToken)
}


inline fun <reified T: Tokenized<*>, TT: Any> Collection<Tokenized<*>>.filterTokenHolder(
    paramClass: KClass<TT>
): List<T>{

    val baseClass = T::class
    val filtered = filterIsInstance<T>()
    val result = mutableListOf<T>()

    filtered.forEach {tokenized->
        if(paramClass in tokenized.typeToken.inlinedParameters){
            result.add(tokenized)
        }
    }
//    val typeFiltered = filtered.mapNotNull { tokenHolder ->
//        if(tokenHolder.typeToken.strictEquality(baseClass, paramClass)){
//            tokenHolder
//        }else{
//            null
//        }
//    }
    return result
}

inline fun <reified T: Tokenized<TT>, TT: Any> Collection<Tokenized<*>>.filterTokenized(
    paramClass: KClass<TT>
): List<T>{
    val filtered = filterIsInstance<Tokenized<TT>>()
    val typeFiltered = filtered.mapNotNull {tokenHolder->
        if(tokenHolder.typeToken == paramClass){
            tokenHolder.safeCast<T>()
        }else{
            null
        }
    }
    return typeFiltered
}