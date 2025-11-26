package po.misc.reflection

import po.misc.exceptions.throwableToText
import po.misc.types.ClassAware
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



fun <T: Any> KProperty<*>.resolveTypedProperty(
    kind: Readonly,
    receiverClass: KClass<*>,
    returnType: ClassAware<T>,
): KProperty1<Any, T>?{

   return receiverClass.memberProperties.firstOrNull { it.name == this.name }?.let {found->
       val casted = found.safeCast<KProperty1<Any, T>>()
       casted
    }?:run {
        null
    }
}

fun <T: Any> KProperty<*>.resolveTypedProperty(
    kind: Readonly,
    receiverClass: ClassAware<*>,
    returnType: ClassAware<T>,
): KProperty1<Any, T>?{

    return receiverClass.memberProperties.firstOrNull { it.name == this.name }?.let {found->
        val casted = found.safeCast<KProperty1<Any, T>>()
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











