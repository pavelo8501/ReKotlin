package po.misc.types.token

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.strings.appendGroup
import po.misc.data.strings.appendLine
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import po.misc.types.ClassAware
import po.misc.types.ClassHierarchyMap
import po.misc.types.TypeHolder
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.k_class.toKeyParams
import po.misc.types.requireNotNull
import po.misc.types.safeCast
import kotlin.collections.forEach
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.typeOf
import kotlin.toString


/**
 * Represents a reflective container for Kotlin type information that preserves
 * both its runtime [KClass] identity and compile-time [KType] metadata,
 * including generic parameters and nullability.
 *
 * ### Overview
 * `TypeToken` provides a way to carry rich type information — such as
 * generic arguments or nullability — across runtime boundaries where
 * type erasure would normally prevent it.
 *
 * This class allows type-safe equality checks, generic inference validation,
 * and runtime-safe casts using the preserved [kClass] reference.
 *
 * It differs from most reflective wrappers in that it:
 * - Enforces a non-nullable base type ([T : Any]) to guarantee that [kClass] is always valid.
 * - Tracks nullability separately through [kType].
 * - Immediately resolves generic slots ([TypeSlot]) from both [KClass] and [KType]
 *   on initialization for safe structural comparison and introspection.
 *
 * ### Core Responsibilities
 * - Preserve full reflective metadata of a Kotlin type.
 * - Provide stable structural and strict equality checks between tokens.
 * - Allow creation of nullable equivalents while maintaining base [KClass].
 * - Offer formatted names and debugging output for analysis or logging.
 *
 * ### Example
 * ```kotlin
 * val token = TypeToken.create<Map<String, Int>>()
 * println(token.typeName) // Map<String, Int>
 * println(token.isNullable) // false
 *
 * val nullable = token.nullable<Map<String, Int>?>()
 * println(nullable.isNullable) // true
 *
 * val sameClass = token.equals(Map::class) // true
 * val strictEqual = token.stricterEquality(nullable) // true
 * ```
 *
 * ### Equality Modes
 * - **equals()** — compares only the [KClass] identity.
 * - **stricterEquality()** — compares [KClass] plus all resolved generic parameters.
 *
 * ### Generic Slot Resolution
 * Each generic argument is represented by a [TypeSlot], containing:
 * - The parameter name and its declaration site.
 * - The resolved [KClass] of the argument.
 * - The raw [KType] reference for advanced usage.
 *
 * ### Companion Factory
 * `TypeToken.create<T>()` provides reified creation using compile-time type inference.
 *
 * @param T The reified type being tokenized. Must be non-nullable.
 * @property kClass The runtime [KClass] of the represented type.
 * @property kType The reflective [KType], including generic arguments and nullability.
 * @property typeSlots The resolved list of generic argument slots.
 * @property isNullable Whether the represented type is marked nullable.
 * @property inlinedParameters A list of all resolved generic argument classes.
 *
 * @see TypeSlot
 * @see GenericInfo
 * @see ClassResolver
 */
class TypeToken<T>  @PublishedApi internal constructor(
    override val kClass: KClass<T & Any>,
    override val kType: KType,
): TypeHolder<T>, TraceableContext, PrettyPrint {


    override val isCollection: Boolean get() = kType.classifier == List::class
    private val nullabilitySign get() = if (isNullable) "?" else ""
    private val slotsText: String
        get() {
            return if (typeSlots.isNotEmpty()) {
                typeSlots.joinToString(prefix = "<", postfix = ">", separator = ", ") {
                    it.toString()
                }
            } else {
                SpecialChars.EMPTY
            }
        }

    private val slotsFormattedText: String
        get() =
            if (typeSlots.isNotEmpty()) {
                typeSlots.joinToString(prefix = "<", postfix = ">", separator = ", ") {
                    it.formattedString
                }
            } else {
                SpecialChars.EMPTY
            }

    var verbosity: Verbosity = Verbosity.Warnings

    val isNullable: Boolean get() = kType.isMarkedNullable
    val typeSlots: List<TypeSlot> = tryResolveImmediately()
    val inlinedParameters: List<KClass<*>> = typeSlots.mapNotNull { it.kClass }.sortedBy { simpleName }

    val hashCode: Int = kClass.hashCode()

    val simpleName: String get() = "${kClass.simpleOrAnon}$nullabilitySign"

    private val tokenName: String
        get() {
            return if (typeSlots.isNotEmpty()) {
                "TypeToken of $simpleName$slotsText"
            } else {
                "TypeToken of $simpleName"
            }
        }
    private val tokenFormattedName: String
        get() {
            return if (typeSlots.isNotEmpty()) {
                "TypeToken of $simpleName$slotsFormattedText"
            } else {
                "TypeToken of $simpleName"
            }
        }
    val typeName: String get() = "$simpleName$slotsText"
    override val formattedString: String
        get() {
            return tokenFormattedName
        }
    private fun tryResolveImmediately(): List<TypeSlot> {
        val result: MutableList<TypeSlot> = mutableListOf()
        val typeParameters: List<KTypeParameter> = kClass.typeParameters
        kType.arguments.forEachIndexed { index, arg ->
            val parameter = typeParameters.getOrNull(index) ?: return@forEachIndexed
            val hostSlot = TypeSlot(parameter)
            arg.type?.let { argType ->
                when (val classifier = argType.classifier) {
                    is KClass<*> ->{
                        hostSlot.resolve(TypeToken(classifier, argType))
                    }
                    is KTypeParameter ->{
                       val typeSlot = TypeSlot(classifier)
                       result.add(typeSlot)
                    }
                }
            }
            result.add(hostSlot)
        }
        return result
    }
    private fun warnKClassDifferent(other: KClass<*>, methodName: String) {
        val line1 = kClass.toKeyParams()
        val line2 = other.toKeyParams()
        val warnMsg = "Comparison failed when comparing own" + SpecialChars.NEW_LINE + "$line1 to " + "$line2"
        notify(warnMsg, methodName, NotificationTopic.Warning)
    }

    fun effectiveClassIs(other: KClass<*>): Boolean {
       return if(isCollection){
            typeSlots.firstOrNull()?.let {
                it.kClass == other
            }?:false
        }else{
            equals(other)
        }
    }
    override fun hashCode(): Int =  kType.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other != null) {
            return when (other) {
                is TypeToken<*> -> kType == other.kType
                is KClass<*> -> kClass == other
                else -> false
            }
        }
        return false
    }

    private fun makeStrictEquality(otherClass: KClass<*>, parameters: List<Pair<KClass<*>, Boolean?>>): Boolean {
        if (kClass != otherClass) {
            warnKClassDifferent(otherClass, "stricterEquality")
            return false
        }
        if (typeSlots.size != parameters.size) {
            return false
        }
        val classList  = typeSlots.mapNotNull { it.kClass }
        parameters.forEach {
            if( it.first !in classList){
                return false
            }
        }
        return true
    }

    private fun slotsContainClass(otherClass: KClass<*>): Boolean {
       return typeSlots.any { it.kClass == otherClass }
    }

    /**
     * Performs a strict equality check between this [TypeToken] and another type.
     *
     * This comparison verifies both the [KClass] identity and the generic type parameters.
     *
     * ### Behavior
     * - When comparing against another [TypeToken], both **type arguments** and their **nullability markers**
     *   are compared — this provides **full Kotlin type precision** (e.g., `String` ≠ `String?`).
     * - When comparing against a raw [KClass], nullability **cannot** be verified,
     *   since [KClass] does not retain nullability information. In that case,
     *   only the raw class and its generic type structure are compared.
     *
     * ### Examples
     * ```
     * val t1 = TypeToken.create<List<String>>()
     * val t2 = TypeToken.create<List<String?>>()
     * val t3 = TypeToken.create<List<String>>()
     *
     * t1.strictEquality(t2) // false — nullability differs
     * t1.strictEquality(t3) // true
     *
     * t1.strictEquality(List::class, String::class) // true, but ignores nullability
     * ```
     *
     * @param other another [TypeToken] or [KClass] to compare against
     * @return `true` if both type and generic arguments (including nullability, when available) match
     */
    fun strictEquality(other: KClass<*>, vararg typeParameters: KClass<*>): Boolean =
        makeStrictEquality(other, typeParameters.toList().map { Pair(it, null) })

    /**
     * Performs a strict equality check between this [TypeToken] and another type.
     *
     * This comparison verifies both the [KClass] identity and the generic type parameters.
     *
     * ### Behavior
     * - When comparing against another [TypeToken], both **type arguments** and their **nullability markers**
     *   are compared — this provides **full Kotlin type precision** (e.g., `String` ≠ `String?`).
     * - When comparing against a raw [KClass], nullability **cannot** be verified,
     *   since [KClass] does not retain nullability information. In that case,
     *   only the raw class and its generic type structure are compared.
     *
     * ### Examples
     * ```
     * val t1 = TypeToken.create<List<String>>()
     * val t2 = TypeToken.create<List<String?>>()
     * val t3 = TypeToken.create<List<String>>()
     *
     * t1.strictEquality(t2) // false — nullability differs
     * t1.strictEquality(t3) // true
     *
     * t1.strictEquality(List::class, String::class) // true, but ignores nullability
     * ```
     *
     * @param other another [TypeToken] or [KClass] to compare against
     * @return `true` if both type and generic arguments (including nullability, when available) match
     */
    fun strictEquality(other: TypeToken<*>): Boolean =
        makeStrictEquality(other.kClass, other.typeSlots.mapNotNull { it.toComparePair() })

    /**
     * Compares this [TypeToken] to a given base type [baseClass] and its type arguments,
     * determining whether they represent the same generic structure.
     *
     * This function performs a **partial** equality check — meaning it requires the base
     * class to match exactly, but allows for flexible comparison of type arguments by ensuring
     * that each [parameter] exists in [inlinedParameters].
     *
     * @param baseClass The base [KClass] of the target type to check against.
     * @param parameter One or more [KClass] values representing expected generic type arguments.
     * @return `true` if the base class matches and all provided parameters exist in the
     *         [inlinedParameters]; otherwise, `false`.
     *
     * Example:
     * ```
     * val token = TypeToken.create<DSLParameterGroup<Test, Int>>()
     * val matches = token.partialEquality(DSLParameterGroup::class, Int::class)
     * assertTrue(matches) // ✅
     * ```
     */
    fun partialEquality(baseClass: KClass<out T & Any>, vararg parameter: KClass<*>): Boolean {
        if (this == baseClass) {
            val testAgainst = parameter.toList()
            testAgainst.forEach {
                if (it !in inlinedParameters) {
                    return false
                }
            }
            return true
        }
        return false
    }

    fun partialEquality(token: TypeToken<*>): Boolean = slotsContainClass(token.kClass) || this == token


    fun typeSlotMatch(litera: TypeLitera, kClass: KClass<*>): Boolean {
        return typeSlots.firstOrNull { it.typeLitera == litera }?.let {
            it.kClass == kClass
        } ?: false
    }

    fun equality(baseClass: KClass<T & Any>, vararg parameter: KClass<*>): Boolean {
        if (this == baseClass) {
            val testAgainst = parameter.toList()
            testAgainst.forEach {
                if (it !in inlinedParameters) {
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun toString(): String {
        return buildString {
            appendGroup("$tokenName [", "", ::hashCode, ::isNullable)
            append(" TypeSlots count : ${typeSlots.size} ]")
        }
    }


    companion object {
        val errMsg: (KClass<*>) -> String = {
            "Impossible to create token for type ${it.simpleName}. KClass<T> should be non nullable"
        }
//        @PublishedApi
//        internal fun buildSlotTree(
//            type: KType,
//            parameter: KTypeParameter? = null
//        ): TypeSlot {
//
//            val kClass = type.classifier as? KClass<*>
//                ?: error("Unsupported classifier: $type")
//            val params = kClass.typeParameters
//            val slots = type.arguments.mapIndexedNotNull { index, arg ->
//                val argType = arg.type ?: return@mapIndexedNotNull null
//                val param = params.getOrNull(index)
//                buildSlotTree(argType, param)
//            }
//            return TypeSlot(type, kClass, parameter, slots)
//        }

        inline operator fun <reified T> invoke(tokenOptions: TokenOptions? = null): TypeToken<T> {
            val casted = T::class.safeCast<KClass<T & Any>>()
            requireNotNull(casted) { errMsg(T::class) }
            val kType = typeOf<T>()
            return TypeToken(casted, kType)
        }

        inline fun <reified T> create(options: TokenOptions? = null): TypeToken<T> {
            val casted = T::class.safeCast<KClass<T & Any>>()
            requireNotNull(casted) { errMsg(T::class) }
            val kType = typeOf<T>()
            return TypeToken(casted, kType)
        }

        inline fun <T, reified GT : T?> create(
            baseClass: KClass<T & Any>,
            options: TokenOptions? = null
        ): TypeToken<T> {
            val casted = GT::class.safeCast<KClass<T & Any>>()
            requireNotNull(casted) { errMsg(GT::class) }
            val kType = typeOf<GT>()
            return TypeToken(casted, kType)
        }
    }
}
