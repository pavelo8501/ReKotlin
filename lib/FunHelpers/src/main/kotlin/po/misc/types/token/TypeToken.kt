package po.misc.types.token

import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.logging.Verbosity
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.GenericInfo
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.helpers.toKeyParams
import po.misc.types.safeCast
import kotlin.collections.forEach
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.typeOf


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
 * ### Notes
 * - The [nullable] function returns a new token that reflects nullability in [kType]
 *   but preserves the original non-null [kClass].
 * - Intended for advanced use in reflective frameworks, type registries,
 *   or runtime mappers where erased types must be reconstructed.
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
    val kClass: KClass<T & Any>,
    val kType: KType
): Component {

    override val componentID: ComponentID = componentID("TypeToken[$simpleName]", Verbosity.Warnings)

    val isNullable: Boolean get() = kType.isMarkedNullable
    val typeSlots: List<TypeSlot> = tryResolveImmediately(kClass.typeParameters)
    val inlinedParameters: List<KClass<*>> = typeSlots.map { it.genericInfo.classInfo.kClass }.sortedBy { simpleName }

    val hashCode: Int = kClass.hashCode()
    val simpleName : String get() {
       return if(isNullable){
            "${kClass.simpleOrAnon}?"
        }else{
            kClass.simpleOrAnon
        }
    }

    private val formatedTypeString: String get() {
       return if(typeSlots.isNotEmpty()){
            typeSlots.joinToString(prefix = "<", separator = ", ", postfix = ">") {
                it.formattedString
            }
        }else{ SpecialChars.EMPTY }
    }

    val typeName: String get() = simpleName.colorize(Colour.Yellow) + formatedTypeString

    private fun tryResolveImmediately(typeParameters:  List<KTypeParameter>):List<TypeSlot>{
        val result: MutableList<TypeSlot> = mutableListOf()
        kType.arguments.forEachIndexed { index, arg ->
            val parameter =  typeParameters.getOrNull(index)?: return@forEachIndexed
            arg.type?.let { argType ->
                (argType.classifier as? KClass<*>)?.let { klass ->
                    val genericParam = GenericInfo(parameter.name, argType,  ClassResolver.classInfo(klass))
                    val typeSlot = TypeSlot(genericParam, parameter)
                    result.add(typeSlot)
                }
            }
        }
        return result
    }

    private fun warnKClassDifferent(other: KClass<*>, methodName: String){
        val line1 = kClass.toKeyParams()
        val line2 = other.toKeyParams()
        val warnMsg = "Comparison failed when comparing own"+ SpecialChars.NEW_LINE + "$line1 to " + "$line2"
        warn(warnMsg, methodName)
    }

    override fun hashCode(): Int = kClass.hashCode()

    override fun equals(other: Any?): Boolean {
        if(other != null){
            var result : Boolean = false

            when(other){
                is TypeToken<*>->{
                    result =   kClass == other.kClass
                    if(!result){
                        warnKClassDifferent(other.kClass, "equals")
                    }
                }
                is KClass<*>->{
                    result = kClass == other
                    if(!result){
                        warnKClassDifferent(other, "equals")
                    }
                }
                else -> false
            }
            return result
        }
        return false
    }

    private fun makeStrictEquality(otherClass: KClass<*>, parameters: List<CompereContainer>): Boolean{
        if (kClass != otherClass){
            warnKClassDifferent(otherClass, "stricterEquality")
            return false
        }
        if(typeSlots.size != parameters.size){
            return false
        }
        for (slot in typeSlots){
            parameters.firstOrNull{  it ==  slot.kClass}?.let {
                if(it.nullable != null &&  slot.isMarkedNullable != it.nullable){
                    return false
                }
            }?:run {
                return false
            }
        }
        return true
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
    fun strictEquality(other: KClass<*>, vararg typeParameters:KClass<*>): Boolean = makeStrictEquality(other, typeParameters.toList().map { CompereContainer(it, null) })

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
    fun strictEquality(other: TypeToken<*>): Boolean = makeStrictEquality(other.kClass, other.typeSlots.map { it.toCompareContainer() })

    fun printSlots(){
        typeSlots.joinToString(prefix = "[", postfix = "]", separator = ", ") {
            it.toString()
        }
    }

    override fun toString(): String {
        return buildString {
            appendLine("SimpleName:$simpleName")
            appendLine("TypeName: $typeName")
            appendLine("Hash Code: $hashCode")
            appendLine("Nullable: $isNullable")
            appendLine("TypeSlots : ${typeSlots.size} "+ printSlots())
        }
    }

    companion object{


        inline fun <reified T> create():TypeToken<T>{
            val casted = T::class.safeCast<KClass<T & Any>>()
            if(casted != null){
                return  TypeToken<T>(casted,  typeOf<T>())
            }else{
                val errMsg = "Impossible to create token for type ${T::class.simpleName}. KClass<T> should be non nullable"
                throw IllegalArgumentException(errMsg)
            }
        }

        inline fun <T, reified GT: T?> create(baseClass: KClass<T & Any>): TypeToken<T>{
            return  TypeToken(baseClass, typeOf<GT>())
        }

        inline fun <reified T, reified GT: T?> createPrecise(): TypeToken<T>{
            return  T::class.safeCast<KClass<T & Any>>()?.let {
                TypeToken(it, typeOf<GT>())
            }?:run {
                val errMsg = "Impossible to create token for type ${T::class.simpleName}. KClass<T> should be non nullable"
                throw IllegalArgumentException(errMsg)
            }
        }
    }
}

/**
 * Creates a new [TypeToken] instance representing a nullable version of the current type.
 *
 * The resulting token preserves the same non-nullable [kClass] identity but updates
 * its [KType] metadata to reflect nullability. This makes it safe to use in
 * casting, logging, or reflection scenarios where nullable and non-nullable
 * types must be distinguished.
 *
 * Example:
 * ```kotlin
 * val stringToken = TypeToken.create<String>()
 * val nullableStringToken = stringToken.nullable<String?>()
 *
 * println(nullableStringToken.isNullable) // true
 * ```
 *
 * @param NT A nullable type bound by the base [T].
 * @return A new [TypeToken] representing a nullable variant of this type.
 */
inline fun <T: Any, reified NT : T?> TypeToken<T>.nullable():TypeToken<T>{
    return TypeToken(kClass, typeOf<NT>())
}


infix  fun List<KClass<*>>.typeClassesAlign(typeToken : TypeToken<*>): Boolean{
    val thisSorted = sortedBy { it.simpleName }
    val typeTokenClasses = typeToken.inlinedParameters

    return thisSorted == typeTokenClasses
}

infix fun TokenHolder.sameBaseClass(other: TokenHolder): Boolean =
    this.typeToken.kClass == other.typeToken.kClass
