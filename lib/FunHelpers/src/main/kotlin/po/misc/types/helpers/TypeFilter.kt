package po.misc.types.helpers

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.styles.SpecialChars
import po.misc.types.k_class.KClassParam
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.k_class.toKeyParams
import po.misc.types.safeCast
import po.misc.types.token.TokenHolder
import po.misc.types.token.TypeToken
import kotlin.collections.mapNotNull
import kotlin.reflect.KClass


data class Filtration(
    val reifiedClass: KClassParam,
    val initialCollectionSize: Int
): PrettyPrint{

    class FilterOperation(val name: String){
        var comment: String = ""
        var result: Boolean = false
        val comments: MutableList<String> = mutableListOf()

        private val commentsString get() =  comments.joinToString(separator = ",") { it }

        fun addComment(comment: String): FilterOperation{
            comments.add(comment)
            return this
        }
        fun registerResult(result: Boolean):Boolean{
            this.result = result
            return result
        }
        fun printout(): String{
            return toString() + SpecialChars.NEW_LINE + "Comments: $commentsString"
        }
        override fun toString(): String {
          return "$name [Result : $result]"
        }
    }
    override val formattedString: String get() = printout()
    internal val operations = mutableListOf<FilterOperation>()
    internal var filtrationSize: Int = initialCollectionSize

    fun registerOperation(name: String, comment: String, result: Boolean):FilterOperation{
       val operation = FilterOperation(name)
       operation.comment = comment
       operation.result = result
       operations.add(operation)
       return operation
    }

    fun <R> startOperation(name: String, operationLambda: (FilterOperation)-> R):R{
        val operation = FilterOperation(name)
        val result =  operationLambda.invoke(operation)
        operations.add(operation)
        return result
    }

    fun sizeAfterFiltration(size: Int):Filtration{
        filtrationSize = size
        return this
    }

    fun printout(): String{
       return "${toString()} " + SpecialChars.NEW_LINE + operations.joinToString(separator = SpecialChars.NEW_LINE) {
            it.printout()
        }
    }

    override fun toString(): String {
       return "Filtration[ ClassName: ${reifiedClass.simpleName} " +
               "Initial Size: $initialCollectionSize Filtered Size $filtrationSize"
    }
}

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
internal fun <T: Any> typedFiltrator(
    collection: List<*>,
    expectedClass: KClass<T>,
    typeParameters: List<KClass<*>>
): List<T> {

    val filtrationData = Filtration(expectedClass.toKeyParams(), collection.size)
    val filteredByRuntimeType = collection.mapNotNull { element ->
         element?.safeCast<T>(expectedClass) ?: return@mapNotNull null
    }

    val comment = "Filtered size: ${filteredByRuntimeType.size}"
    val byRuntimeTypeResult =  filteredByRuntimeType.size == collection.size
    val operation = filtrationData.registerOperation("Filter by runtime type", comment, byRuntimeTypeResult)
    if (typeParameters.isEmpty()){
        filtrationData.sizeAfterFiltration(filteredByRuntimeType.size)
        operation.registerResult(byRuntimeTypeResult)
        return filteredByRuntimeType
    }

    val typeFiltered = filteredByRuntimeType.filter { candidate ->
        if (candidate !is TokenHolder) {
            filtrationData.registerOperation("Filter by typeParameters", "Skipped. Candidate is not implementing Tokenized", false)
            return@filter false
        }
       filtrationData.startOperation("Comparing type parameters provided with token parameters"){operation->
            val token = candidate.typeToken
            operation.addComment("Candidates tokens type parameters: ${token.inlinedParameters}")
            val required = typeParameters.sortedBy { it.simpleName }
            val requiredAsString = required.joinToString(separator = ", ") { it.simpleOrAnon }
            operation.addComment("Provided type parameters: $requiredAsString")
            operation.registerResult(token.inlinedParameters.containsAll(required))
        }
    }
    filtrationData.sizeAfterFiltration(typeFiltered.size)
    if(typeFiltered.isEmpty()){
        filtrationData.output()
    }
    return typeFiltered
}

/**
 * Filters this list, returning only elements of type [T] that optionally match
 * a set of provided generic [typeParameters].
 *
 * This function performs reflection-based filtering using [TypeToken] metadata
 * when available (i.e., if the element implements [TokenHolder]).
 *
 * ### Example
 * ```kotlin
 * val list: List<Any> = createHoldersComponentInt(3)
 *
 * // Match only Tokenized elements representing TypeHolder2<ComponentInt, SealedInheritor>
 * val filtered = list.filterByType<TypeHolder2<ComponentInt, SealedInheritor>>(
 *     ComponentInt::class, SealedInheritor::class
 * )
 * ```
 *
 * ### Notes
 * - If [T] implements [TokenHolder], you **must** provide matching [typeParameters].
 *   Forgetting to do so will result in an [IllegalStateException].
 * - If [T] does **not** implement [TokenHolder], [typeParameters] are ignored.
 *
 * @param typeParameters optional runtime type parameters to match against.
 * @return filtered list containing only instances of [T] that match the given criteria.
 *
 * @see filterByTypeWhere
 * @see typedFiltrator
 */
inline fun <reified T: Any> List<*>.filterByType(vararg typeParameters: KClass<*>): List<T>
    = typedFiltrator(this, T::class, typeParameters.toList())



/**
 * Filters this list by matching both the **runtime class** of elements and the
 * **parameterized type tokens** of their generic arguments.
 *
 * This overload is designed for cases where a simple `KClass` comparison is
 * insufficient — for example, when generic type parameters differ at runtime
 * but share the same base class.
 *
 * Example:
 * ```kotlin
 * val buttons: List<FormButton<*, *>> = ...
 * val typeToken = TypeToken.create<FormButton<TextView<String>, MyUpdate>>()
 *
 * val textButtons = buttons.filterByType<FormButton<TextView<String>, MyUpdate>>(typeToken)
 * ```
 *
 * @param typeTokens one or more [TypeToken]s describing the desired type parameters
 *                   of the target type `T`. Each token’s [TypeToken.kClass] will be
 *                   compared against the parameterized types of the inspected elements.
 *
 * @return a new list containing only elements that are instances of [T] **and**
 *         whose generic type arguments match all provided [typeTokens].
 *
 * @see TypeToken
 * @see typedFiltrator
 * @see filterByType for a simpler `KClass`-only variant
 */
@JvmName("filterByTypeAndToken")
inline fun <reified T: Any> List<*>.filterByType(
    mandatoryOne: TypeToken<*>,
    vararg typeTokens: TypeToken<*>
): List<T> {
    val classList =  typeTokens.map { it.kClass }.toMutableList()
    classList.add(mandatoryOne.kClass)
    return typedFiltrator(this, T::class, classList)
}

/**
 * Variant of [filterByType] that allows specifying an additional [predicate]
 * to filter the resulting elements based on arbitrary user-defined logic.
 *
 * ### Example
 * ```kotlin
 * val filtered = list.filterByTypeWhere<TypeHolder2<ComponentInt, SealedInheritor>>(
 *     ComponentInt::class, SealedInheritor::class
 * ) { holder ->
 *     holder.value > 10
 * }
 * ```
 *
 * @param typeParameters optional runtime type parameters to match against.
 * @param predicate an additional condition applied after successful casting and type matching.
 * @return filtered list of type [T] satisfying both type and predicate conditions.
 *
 * @see filterByType
 * @see typedFiltrator
 */
inline fun <reified T: Any> List<*>.filterByTypeWhere(
    vararg typeParameters: KClass<*>,
    predicate: (T)-> Boolean
): List<T> = typedFiltrator(this, T::class, typeParameters.toList()).filter(predicate)


/**
 * Filters the elements of this list by a target type [T] and additional runtime type constraints,
 * returning only those elements that match the given type conditions and satisfy [predicate].
 *
 * This overload allows specifying a **mandatory** [TypeToken] ([mandatoryOne]) that defines
 * at least one runtime type to compare against. This helps disambiguate type inference and
 * guarantees that the filtering always has a concrete comparison target.
 *
 * The resulting list will only contain elements that:
 * 1. Are assignable to the reified type parameter [T], and
 * 2. Match any of the runtime types in the combined list of [mandatoryOne] and [typeTokens],
 * 3. Satisfy the provided [predicate].
 *
 * @param T the target reified type to which elements will be safely cast.
 * @param mandatoryOne the required [TypeToken] used as the primary comparison type
 *                     when resolving runtime type compatibility.
 * @param typeTokens optional additional [TypeToken]s to compare element types against.
 * @param predicate an additional filter applied to each element after type matching.
 * @return a list of elements of type [T] that satisfy both type and predicate conditions.
 *
 * @see TypeToken for representing reified generic type information at runtime.
 * @see typedFiltrator for the underlying reflective filtering mechanism.
 *
 * Example usage:
 * ```
 * val buttons = formButtons.filterByTypeWhere<TextButton>(
 *     mandatoryOne = TypeToken.create<TextReply>(),
 *     TypeToken.create<DocumentReply>()
 * ) { it.isActive }
 * ```
 * This returns only `TextButton` instances that are compatible with `TextReply`
 * and `DocumentReply`, and for which `isActive` is `true`.
 */
inline fun <reified T: Any> List<*>.filterByTypeWhere(
    mandatoryOne: TypeToken<*>,
    vararg typeTokens: TypeToken<*>,
    predicate: (T)-> Boolean
): List<T> = filterByType<T>(mandatoryOne, *typeTokens).filter(predicate)


@JvmName("filterByTypeNonReified")
fun <T: Any>  List<*>.filterByType(
    typeToken: TypeToken<T>,
): List<T> = typedFiltrator<T>(this.toList(), typeToken.kClass, typeToken.inlinedParameters)


fun <T: Any, P: Any>  Iterable<*>.filterByTypeAndToken(
    expectedClass: KClass<T>,
    typeToken: TypeToken<P>,
): List<T> = typedFiltrator<T>(this.toList(), expectedClass, listOf(typeToken.kClass))


