package po.misc.reflection

import po.misc.data.toDisplayName
import po.misc.exceptions.throwableToText
import po.misc.types.ClassAware
import po.misc.types.castOrThrow
import po.misc.types.memberProperties
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


val  KProperty0<Any>.nameValuePair : NameValuePair get() {
    val value = this.get()
    return NameValuePair(this.name, value.toString())
}

val <T: Any> KProperty1<T, Any>.nameValuePair: Pair<String,  KProperty1<T, Any>>   get() {
    return  Pair(this.name, this)
}


val KProperty<*>.displayName: String  get() {
    return this.name.toDisplayName()
}


fun KProperty<*>.createPropertyLookup():PropertyLookup{
    val receiverClass = this.getter.call()
    if(receiverClass != null){
        return PropertyLookup(receiverClass::class)
    }else{
        return PropertyLookup(null)
    }
}

fun KProperty1<Any, *>.readValueAsString(receiver: Any, onFailureAction : ((PropertyLookup)-> Unit)? = null): String {
    val lookup = createPropertyLookup()
    val receiverClass = receiver::class
    return  try {
       get(receiver).toString()
    }catch (ex: Throwable){
        lookup.registerThrowable(ex)
        onFailureAction?.invoke(lookup)
        ex.throwableToText()
    }
}



inline fun <reified T: Any> KProperty0<*>.tryResolveToReadOnly(): KProperty1<T, *>? {
    val kClass = T::class
    return kClass.memberProperties.firstOrNull { it.name == this.name }
}

inline fun <reified T: Any> KProperty0<*>.tryResolveToMutable(): KMutableProperty1<T, *>? {
    val kClass = T::class

    return kClass.memberProperties.firstOrNull { it.name == this.name }?.safeCast<KMutableProperty1<T, *>>()
}


/**
 * Represents the kind of Kotlin property being resolved using reflection.
 *
 * `ReadOnlyProperty` corresponds to [KProperty1],
 * `MutableProperty` corresponds to [KMutableProperty1].
 */
sealed interface PropertyKind

object Readonly  : PropertyKind
object Mutable  :PropertyKind



/**
 * Attempts to resolve a member property of this object as a read-only [KProperty1].
 *
 * @param kind requires [Readonly] to match function overload.
 * @param property the reflective property reference, typically obtained from `::myProp`.
 * @return the resolved [KProperty1] if the property exists and is read-only, otherwise `null`.
 */
fun <T: Any> T.resolveProperty(
    kind: Readonly,
    property: KProperty<*> ,
): KProperty1<T, *>? {
    val kClass = this::class
   return kClass.memberProperties.firstOrNull { it.name == property.name }?.safeCast<KProperty1<T, *>>()
}

fun <T: Any> resolveProperty(
    kind: Readonly,
    kClass: KClass<T>,
    property: KProperty<*>,
): KProperty1<T, *>? {
    return kClass.memberProperties.firstOrNull { it.name == property.name }?.safeCast<KProperty1<T, *>>()
}

fun <T: Any> resolveProperty(
    kind: Readonly,
    classAware: ClassAware<T>,
    property: KProperty<*>,
): KProperty1<T, *>? = resolveProperty(kind, classAware.kClass, property)




fun <T: Any, V: Any> KProperty<*>.resolveTypedProperty(
    kind: Readonly,
    receiverClass: KClass<T>,
    returnType: ClassAware<V>,
): KProperty1<T, V>?{

   return receiverClass.memberProperties.firstOrNull { it.name == this.name }?.let {found->
       val casted = found.safeCast<KProperty1<T, V>>()
       casted
    }?:run {
        null
    }
}

fun <T: Any, V: Any> KProperty<*>.resolveTypedProperty(
    kind: Readonly,
    receiverClass: ClassAware<T>,
    returnType: ClassAware<V>,
): KProperty1<T, V>?{

    return receiverClass.memberProperties.firstOrNull { it.name == this.name }?.let {found->
        val casted = found.safeCast<KProperty1<T, V>>()
        casted
    }?:run {
        null
    }
}



/**
 * Attempts to resolve a member property of this object as a mutable [KMutableProperty1].
 *
 * @param kind requires [Mutable] to match function overload.
 * @param property the reflective property reference, typically obtained from `::myProp`.
 * @return the resolved [KMutableProperty1] if the property exists and is mutable, otherwise `null`.
 */
fun <T: Any> T.resolveProperty(
    kind: Mutable ,
    property: KProperty<*>,
): KMutableProperty1<T, *>? {
    val kClass = this::class
    return kClass.memberProperties.firstOrNull { it.name == property.name }?.safeCast<KMutableProperty1<T, *>>()
}

/**
 * Attempts to resolve a property with the same name as [property] on the given [kClass]
 * and safely cast it to a strongly typed `KProperty1<T, V>`.
 *
 * This is useful when the source [property] originates from a different class
 * (e.g., when mapping DTOs or performing reflective copies) but you want to
 * obtain a type-safe reference to the corresponding property on [kClass] without
 * falling back to `KProperty1<Any, V>`.
 *
 * The function:
 *  - searches for a member property named like [property]
 *  - checks its runtime type
 *  - returns it as `KProperty1<T, V>` if compatible
 *  - otherwise returns `null`
 *
 * @param T The expected receiver type of the resolved property.
 * @param V The property value type.
 * @param kind A marker used for resolving read-only vs mutable behaviours.
 * @param kClass The class on which the matching property is searched.
 * @param property The property whose name and type signature are used for resolution.
 *
 * @return A safely casted `KProperty1<T, V>` if a compatible property is found,
 *         or `null` if the property does not exist or is not type-compatible.
 */
inline fun <reified T: Any, reified V: Any> resolveTypedProperty(
    kind: Readonly,
    kClass: KClass<*>,
    property: KProperty<V>,
): KProperty1<T, V>? {
    return kClass.memberProperties.firstOrNull { it.name == property.name }?.safeCast<KProperty1<T, V>>()
}

/**
 * Overload of [resolveTypedProperty] that accepts a [ClassAware] wrapper instead
 * of a direct [KClass]. Useful when the caller already operates on class-aware
 * structures in the mapping system.
 *
 * Delegates to [resolveTypedProperty] using [ClassAware.kClass].
 *
 * @param T The expected receiver type of the resolved property.
 * @param V The property value type.
 * @param kind A marker used for resolving read-only vs mutable behaviours.
 * @param classAware Wrapper providing access to the underlying [KClass] instance.
 * @param property The property whose name and type signature are used for resolution.
 *
 * @return A safely casted `KProperty1<T, V>` or `null`.
 */
inline fun <reified T: Any, reified V: Any> resolveTypedProperty(
    kind: Readonly,
    classAware: ClassAware<*>,
    property: KProperty<V>,
): KProperty1<T, V>? = resolveTypedProperty(kind, classAware.kClass, property)


inline fun <reified T: Any, V: Any> resolveTypedProperty(
    kind: Readonly,
    property: KProperty<V>,
    kClass: KClass<*>,
    typeToken: TypeToken<V>,
): KProperty1<T, V>? {

    val prop = kClass.memberProperties.firstOrNull { it.name == property.name }
    if(prop == null){
        return null
    }
    return with(prop){
        returnClassOrNull(typeToken)?.let {
            safeCast<KProperty1<T, V>>()
        }
    }
}













