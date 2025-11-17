package po.misc.reflection.properties.typed_property

import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.logging.Verbosity
import po.misc.exceptions.managedException
import po.misc.reflection.anotations.AnnotatedProperty
import po.misc.reflection.primitives.BooleanClass
import po.misc.reflection.primitives.IntClass
import po.misc.reflection.primitives.LongClass
import po.misc.reflection.primitives.PrimitiveClass
import po.misc.reflection.primitives.StringClass
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.collections.forEach
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Represents a reflective, type-aware property binding for a receiver type [T].
 *
 * A [TypedProperty] provides uniform read/write access to properties of primitive
 * types (such as `String`, `Int`, or `Long`) via a reflective interface.
 *
 * Implementations (e.g., [StringTypedProperty], [IntTypedProperty]) define how
 * string-based values are converted, validated, and applied to an instance.
 *
 * This interface is particularly useful in scenarios like:
 * - Data-driven UI bindings.
 * - Generic form models.
 * - Reflective serialization or property editing tools.
 *
 * @param T The receiver type whose properties are accessed or mutated.
 * @property typeToken A token describing the receiver type for better debugging
 * and runtime introspection.
 * @property primitiveClass The meta-representation of the property's primitive type.
 */
sealed interface TypedProperty<T: Any>{
    val typeToken: TypeToken<T>
    val primitiveClass: PrimitiveClass<*>
    val property: KProperty<*>

    /**
     * Updates the property's value on the given [receiver] instance.
     *
     * The provided [value] is always a string and should be parsed or
     * directly assigned depending on the property's type.
     *
     * @param receiver The target instance to modify.
     * @param value The new value in string form.
     * @param throwing If `true`, parsing or assignment failures should throw an exception;
     * @return `true` if the value was successfully assigned; `false` otherwise.
     */
    fun updateValue(receiver:T, value: String, throwing: Boolean = false): Boolean

    /**
     * Reads and returns the current value of this property as a string.
     *
     * @param receiver The target instance from which to read the property.
     * @return The property's current value, represented as a string.
     */
    fun readValue(receiver:T): String
}

/**
 * Base class for mutable property bindings backed by [KMutableProperty1].
 *
 * Provides unified logic for:
 *  • Setting values with safety and error handling.
 *  • Reading values reflectively.
 *  • Verbose logging via [Component].
 *
 * @param T The model type containing the property.
 * @param V The primitive or value type of the property.
 * @property property The actual reflective Kotlin property reference.
 */
sealed class MutablePropertyBase<T: Any, V: Any>(
    val property: KMutableProperty1<T, V>
):  Component{

    abstract var verbosity: Verbosity
    abstract var wrongValueMsg: String

    /**
     * Attempts to assign [value] to [receiver]. If [value] is null or assignment
     * fails and [throwing] is `true`, an exception is propagated. Otherwise the
     * failure is logged and `false` is returned.
     */
    fun update(receiver:T, value: V?, throwing: Boolean): Boolean{
        try {
          if(value != null){
              property.set(receiver, value)
              return true
            }else{
                if(throwing){ throw managedException(wrongValueMsg) }
                warn(wrongValueMsg, "updateValue")
              return false
            }
        }catch (th: Throwable){
            if(throwing){
                throw th
            }else{
                warn(wrongValueMsg, "updateValue")
                return false
            }
        }
    }

    /**
     * Reads the raw underlying value of the property from [receiver].
     */
    fun read(receiver:T): V{
        return property.get(receiver)
    }
}

/**
 * Represents a typed, mutable string property of a receiver type [T].
 *
 * Provides reflective access to a `String` property, allowing safe read/write
 * operations through the `KMutableProperty1` API while maintaining type safety
 * via the associated [typeToken].
 *
 * Used to abstractly represent editable properties in data-driven components,
 * form bindings, or generic data manipulation layers.
 *
 * @param T The receiver type that owns the property.
 * @property property The reflective reference to the mutable string property.
 * @property typeToken The type token describing the receiver's class type.
 */
class StringTypedProperty<T: Any>(
    property: KMutableProperty1<T, String>,
    override val typeToken: TypeToken<T>,
): MutablePropertyBase<T, String>(property),  TypedProperty<T> {

    override val primitiveClass: StringClass = StringClass

    override val componentID: ComponentID = componentID("TypedProperty<${typeToken.typeName}, String>")
    override var verbosity: Verbosity
        get() =  componentID.verbosity
        set(value){
            componentID.verbosity = value
        }

    override var wrongValueMsg: String = "Invalid string value"

    override fun updateValue(receiver:T, value: String, throwing: Boolean): Boolean{
       return super.update(receiver, value, throwing)
    }

    override fun readValue(receiver:T): String{
        return read(receiver)
    }
}

/**
 * Represents a typed, mutable integer property of a receiver type [T].
 *
 * Provides reflective access to an `Int` property with string-based update and
 * retrieval, enabling safe parsing and error handling through the [throwing]
 * parameter or internal logging.
 *
 * @param T The receiver type that owns the property.
 * @property property The reflective reference to the mutable integer property.
 * @property typeToken The type token describing the receiver's class type.
 */
class IntTypedProperty<T: Any>(
    property: KMutableProperty1<T, Int>,
    override val typeToken: TypeToken<T>,
): MutablePropertyBase<T, Int>(property),   TypedProperty<T>, Component {
    override val primitiveClass: IntClass = IntClass

    override val componentID: ComponentID = componentID("TypedProperty<${typeToken.typeName}, Int>")
    override var verbosity: Verbosity
        get() =  componentID.verbosity
        set(value){
            componentID.verbosity = value
        }

    override var wrongValueMsg: String = "Invalid integer value"

    /**
     * Attempts to parse the given string [value] into an integer and assign it.
     * Logs or throws if the value cannot be parsed.
     *
     * @param receiver The instance on which to update the property.
     * @param value The string to parse and assign.
     * @param throwing Whether to throw an exception if parsing fails.
     */
    override fun updateValue(receiver:T, value: String, throwing: Boolean): Boolean{
        val intConverted = value.toIntOrNull()
        return update(receiver, intConverted, throwing)
    }

    override fun readValue(receiver:T): String{
       return  read(receiver).toString()
    }
}

/**
 * Represents a typed, mutable long property of a receiver type [T].
 *
 * Provides reflective access to a `Long` property with string-based update and
 * retrieval, including optional exception throwing or logging for invalid input.
 *
 * @param T The receiver type that owns the property.
 * @property property The reflective reference to the mutable long property.
 * @property typeToken The type token describing the receiver's class type.
 */
class LongTypedProperty<T: Any>(
    property: KMutableProperty1<T, Long>,
    override val typeToken: TypeToken<T>,
): MutablePropertyBase<T, Long>(property), TypedProperty<T>, Component {
    override val primitiveClass: LongClass = LongClass

    override val componentID: ComponentID = componentID("TypedProperty<${typeToken.typeName}, Long>")
    override var verbosity: Verbosity
        get() =  componentID.verbosity
        set(value){
            componentID.verbosity = value
        }

    override var wrongValueMsg: String = "Invalid long value"

    override fun updateValue(receiver:T, value: String, throwing: Boolean): Boolean{
        val intConverted = value.toLongOrNull()
        return update(receiver, intConverted, throwing)
    }
    override fun readValue(receiver:T): String{
        return read(receiver).toString()
    }
}

/**
 * A typed, mutable boolean property of a receiver type [T].
 *
 * Provides reflective access to a `Boolean` property with robust string-based parsing.
 * Supports common boolean representations such as `"1"`, `"0"`, `"true"`, `"false"`,
 * `"yes"`, `"no"`, `"checked"`, `"unchecked"`, and `"null"`.
 * Logs or throws an exception for unrecognized values depending on [throwing].
 *
 * @param T The receiver type that owns the property.
 * @property property The reflective reference to the mutable boolean property.
 * @property typeToken The type token describing the receiver's class type.
 */
class BooleanTypedProperty<T: Any>(
    property: KMutableProperty1<T, Boolean>,
    override val typeToken: TypeToken<T>,
):MutablePropertyBase<T, Boolean>(property),  TypedProperty<T>, Component {
    override val primitiveClass: BooleanClass = BooleanClass

    override val componentID: ComponentID = componentID("TypedProperty<${typeToken.typeName}, Boolean>")
    override var verbosity: Verbosity
        get() =  componentID.verbosity
        set(value){
            componentID.verbosity = value
        }

    /** Message logged or thrown when a value cannot be parsed as boolean. */
    override var wrongValueMsg: String = "Invalid boolean value"

    /**
     * Updates the target boolean property by parsing the provided string [value].
     *
     * Recognized inputs (case-insensitive):
     * - `"1"`, `"true"`, `"yes"`, `"checked"` → `true`
     * - `"0"`, `"false"`, `"fals"`, `"no"`, `"unchecked"`, `"null"` → `false`
     * - Any other string → parsed with [String.toBoolean] (`true` if `"true"`, otherwise `false`)
     *
     * Logs a warning or throws an exception if an unrecognized value is encountered,
     * controlled by [throwing].
     *
     * @param receiver The instance on which to update the property.
     * @param value The string to parse and assign.
     * @param throwing Whether to throw an exception for invalid/unrecognized input.
     * @return `true` if the value was successfully assigned; `false` otherwise.
     */
    override fun updateValue(receiver:T, value: String, throwing: Boolean): Boolean{
        val normalized = value.lowercase().trim()
        var reachedElse = false
        val boolConverted = when(normalized){
            "1", "true", "yes", "checked" -> true
            "0", "false", "fals", "no", "unchecked", "null" -> false
            else -> {
                reachedElse = true
                normalized.toBoolean()
            }
        }
        if(reachedElse){
            if(throwing){ throw managedException(wrongValueMsg) }
            warn(wrongValueMsg, "updateValue")
            return false
        }else{
            property.set(receiver, boolConverted)
            return true
        }
    }

    override fun readValue(receiver:T): String{
        return read(receiver).toString()
    }
}


fun <T: Any> KProperty1<T, *>.toTypedProperty(typeToken : TypeToken<T>): TypedProperty<T>? {
    val valueClass = returnType.classifier as KClass<*>
    return when (valueClass) {
        String::class -> {
            safeCast<KMutableProperty1<T, String>>()?.let {
                StringTypedProperty(it, typeToken)
            }
        }
        Int::class -> {
            safeCast<KMutableProperty1<T, Int>>()?.let {
                IntTypedProperty(it, typeToken)
            }
        }
        Long::class -> {
            safeCast<KMutableProperty1<T, Long>>()?.let {
                LongTypedProperty(it, typeToken)
            }
        }
        Boolean::class->{
           safeCast<KMutableProperty1<T, Boolean>>()?.let {
               BooleanTypedProperty(it, typeToken)
           }
        }
        else -> null
    }
}

fun <T: Any> List<KProperty1<T, *>>.toTypedProperties(
    typeToken : TypeToken<T>
): List<TypedProperty<T>> {
    val mutableProperties = filterIsInstance<KMutableProperty1<T, *>>()
    return mutableProperties.mapNotNull {
        it.toTypedProperty(typeToken)
    }
}

/**
 * Converts a list of mutable property references into their corresponding [TypedProperty] wrappers.
 *
 * Each property is introspected for its value type (`String`, `Int`, `Long`, etc.)
 * and mapped to an appropriate reflective wrapper ([StringTypedProperty],
 * [IntTypedProperty], [LongTypedProperty]).
 *
 * This enables type-safe, uniform reflective read/write access across
 * heterogeneous property lists.
 *
 * Example:
 * ```kotlin
 * val props = MyClass::class.memberProperties.toTypedProperties<MyClass>()
 * props.forEach { it.updateValue(instance, "42") }
 * ```
 *
 * @param T The receiver type owning the mutable properties.
 * @receiver The list of property references to be wrapped.
 * @return A list of [TypedProperty] wrappers for each supported primitive property.
 */
inline fun <reified T: Any> List<KProperty1<T, *>>.toTypedProperties(): List<TypedProperty<T>>
        = toTypedProperties(TypeToken.create<T>())


/**
 * Reflectively collects all mutable properties of the given [KClass]
 * and converts them to [TypedProperty] wrappers.
 *
 * Shortcut for:
 * ```kotlin
 * this.memberProperties.toList().toTypedProperties()
 * ```
 *
 * @param T The receiver type whose properties should be converted.
 * @receiver The [KClass] representing the target type.
 * @return A list of [TypedProperty] wrappers.
 */
inline fun <reified T: Any> KClass<T>.toTypedProperties(): List<TypedProperty<T>>
        = memberProperties.toList().toTypedProperties()

@JvmName("toTypedPropertiesOnAnnotationWithToken")
inline fun <T: Any, reified A : Annotation> List<AnnotatedProperty<T, A>>.toTypedProperties(
    typeToken : TypeToken<T>
): List<TypedProperty<T>> =  map { it.property }.toTypedProperties(typeToken)


/**
 * Converts annotated properties into [TypedProperty] wrappers.
 *
 * For each [AnnotatedProperty] entry, extracts the property reference
 * and creates a reflective [TypedProperty] instance matching the property's type.
 *
 * @param T The receiver type whose annotated properties should be converted.
 * @param A The annotation type that marks the properties of interest.
 * @receiver A list of annotated properties.
 * @return A list of [TypedProperty] wrappers corresponding to the annotated properties.
 */
@JvmName("toTypedPropertiesOnAnnotation")
inline fun <reified T: Any, reified A : Annotation> List<AnnotatedProperty<T, A>>.toTypedProperties(
): List<TypedProperty<T>> =  map { it.property }.toTypedProperties()


inline fun <T: Any, reified A: Annotation, R> List<AnnotatedProperty<T, A>>.toTypedProperties(
    typeToken: TypeToken<T>,
    builder: (A, TypedProperty<T>) -> R,
): List<R> {
    val results = mutableListOf<R>()
    forEach {annotated->
        val property = annotated.property
        property.toTypedProperty(typeToken)?.let {typed->
            val result =  builder.invoke(annotated.annotation, typed)
            results.add(result)
        }
    }
    return results
}

inline fun <T: Any, reified A: Annotation, R> List<AnnotatedProperty<T, A>>.toTypedPropertiesSeq(
    typeToken: TypeToken<T>,
    noinline  builder: (A, TypedProperty<T>) -> R
): Sequence<R> = sequence {
    for (annotated in this@toTypedPropertiesSeq) {
        annotated.property.toTypedProperty(typeToken)?.let { typed ->
            yield(builder(annotated.annotation, typed))
        }
    }
}
