package po.misc.properties

import po.misc.types.ClassAware
import po.misc.types.ReflectiveLookup
import po.misc.types.TypeHolder
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

/**
 * Attempts to validate whether the given [property]'s return type is a subtype of [expectedType].
 *
 * This is the core non-inline, non-reified implementation used by all public `checkType` helpers.
 * If the subtype relationship holds, the property is safely cast to `KProperty1<T, V>` and returned.
 * Otherwise, `null` is returned.
 *
 * The optional [lookupReporting] callback can be supplied to receive a [ReflectiveLookup]
 * object containing diagnostic messages about the resolution process – useful for debugging
 * reflective mismatches.
 *
 * ### Type Safety
 * The cast to `KProperty1<T, V>` is performed via `safeCast`, ensuring the function does not throw
 * ClassCastException. A `null` return always means: *"the property cannot be treated as KProperty1<T, V>"*.
 *
 * @param property The property being type-checked.
 * @param expectedType The expected KType of the property’s return value.
 * @param lookupReporting Optional callback receiving detailed reflective diagnostic information.
 *
 * @return `KProperty1<T, V>` if the property's return type is compatible; otherwise `null`.
 */
@PublishedApi
internal fun <T: Any, V> checkPropertyType(
    property: KProperty1<*, *>,
    expectedType: KType,
    lookupReporting: ((ReflectiveLookup) -> Unit)? = null
):KProperty1<T, V>?
{

    val lookup = if(lookupReporting != null) {
        ReflectiveLookup()
    }else{
        null
    }
    val actualType = property.returnType
    lookup?.info("ActualType $actualType")
    if (actualType.isSubtypeOf(expectedType)) {
        lookup?.info("ActualType $actualType isSubtypeOf")
        val casted =  property.safeCast<KProperty1<T, V>>()
        if(casted == null) {
            if(lookup != null){
                lookup.submitResult(false, "Cast to safeCast<KProperty1<T, V>> have failed")
                lookupReporting?.invoke(lookup)
            }
        }else {
            if(lookup != null){
                lookup.submitResult(true, "OK")
                lookupReporting?.invoke(lookup)
            }
        }
        return casted
    }else{
        if(lookup != null){
            lookup.submitResult(false, "ActualType $actualType is no subtype of $expectedType")
            lookupReporting?.invoke(lookup)
        }
        return null
    }
}

/**
 * Checks whether this property can be treated as `KProperty1<T, V>` using a reified receiver [T]
 * and a runtime-supplied expected return type [returnType].
 *
 * This overload is particularly useful when value types are created dynamically,
 * e.g. via TypeToken transformations (`asList()`, `asElement()`, etc.).
 *
 * Example:
 * ```
 * val token = tokenOf<List<String>>()
 *
 * val ok = prop.checkType<MyClass>(token)        // succeeds for List<String>
 * val bad = prop.checkType<MyClass>(tokenOf<Int>()) // fails
 * ```
 *
 * @param returnType Type descriptor for the expected property return type.
 * @param lookupReporting Optional reflective debugging collector.
 */
@JvmName("checkTypeReifiedT")
inline fun <reified T: Any, V> KProperty1<*, *>.checkType(
    returnType: TypeHolder<V>,
    noinline lookupReporting: ((ReflectiveLookup) -> Unit)? = null
): KProperty1<T, V>? = checkPropertyType<T, V>(this,  returnType.kType, lookupReporting)


/**
 * Same as the reified version, but uses an explicit non-reified receiver type.
 *
 * Use this overload when the receiver type [T] is itself determined reflectively or carried
 * through TypeToken-like structures.
 *
 * Only the property’s return type is checked against [returnType]. Receiver compatibility is assumed
 * to be validated earlier in the calling code.
 *
 * @param returnType Type descriptor for the expected return value.
 * @param lookupReporting Optional reflective debugging collector.
 */
fun <T: Any, V> KProperty1<T, *>.checkType(
    returnType: TypeHolder<V>,
    lookupReporting: ((ReflectiveLookup) -> Unit)? = null
): KProperty1<T, V>? = checkPropertyType<T, V>(this,  returnType.kType, lookupReporting)

/**
 * Checks whether this property can be treated as a `KProperty1<T, V>` using fully reified type parameters.
 *
 * This is the most concise and strict overload: both the receiver type [T] and the expected return type [V]
 * are known at compile time. The check succeeds only if:
 *
 * 1. This property is defined on a receiver that is compatible with [T], and
 * 2. The property's return type is a subtype of `V`, and
 * 3. The property can be safely cast to `KProperty1<T, V>`.
 *
 * Example:
 * ```
 * val prop = MyClass::names   // KProperty1<MyClass, List<String>>
 *
 * val ok  = prop.checkType<MyClass, List<String>>()   // succeeds
 * val bad = prop.checkType<MyClass, String>()         // fails
 * ```
 *
 * The optional [lookupReporting] parameter collects diagnostic information about what matched
 * and what failed.
 *
 * @return A safely cast property on success, or `null` on mismatch.
 */
@JvmName("checkTypeFullReified")
inline fun <reified T: Any, reified V> KProperty1<*, *>.checkType(
    noinline lookupReporting: ((ReflectiveLookup) -> Unit)? = null
): KProperty1<T, V>? = checkPropertyType<T, V>(this,  typeOf<V>(), lookupReporting)


/**
 * Performs a property type check using both runtime receiver type information [kClass] and
 * runtime return type information [returnType].
 *
 * This is the most flexible overload: no reified generics are required at all.
 * It is ideal for systems where type information is carried exclusively via reflection
 * (e.g. dynamic grid or DTO mappers).
 *
 * Example:
 * ```
 * val receiverToken = tokenOf<MyClass>()
 * val returnToken   = tokenOf<List<Item>>()
 *
 * val result = prop.checkType(receiverToken, returnToken)
 * ```
 *
 * @param kClass Runtime descriptor for the receiver type [T].
 * @param returnType Runtime descriptor for the expected return type [V].
 * @param lookupReporting Optional reflective debugging collector.
 */
fun <T: Any, V> KProperty1<*, *>.checkType(
    kClass: ClassAware<T>,
    returnType: TypeHolder<V>,
    lookupReporting: ((ReflectiveLookup) -> Unit)? = null
): KProperty1<T, V>? {
    return  checkPropertyType<T, V>(this,  returnType.kType, lookupReporting)
}


