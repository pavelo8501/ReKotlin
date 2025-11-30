package po.misc.reflection

import po.misc.types.ClassAware
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.types.safeClassCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.IllegalCallableAccessException



fun <T: Any,  V: Any> KProperty1<T, V>.getBrutForced(
    returnClass: KClass<V>,
    receiver: T,
    failureReporting: ((PropertyLookup)-> Unit)? = null
) : V {
    val receiverClass = receiver::class
    val lookup: PropertyLookup = PropertyLookup(receiverClass)
    try {
        return get(receiver)
    }catch (visibilityException: IllegalCallableAccessException){
        lookup.registerThrowable(visibilityException)
        val resolved = ReflectiveAssassin.playDirty(receiver, this, lookup)
        return resolved.castOrThrow(returnClass)
    }
    catch (ex: Throwable){
        lookup.registerThrowable(ex)
        throw ex
    }
    finally {
        if(lookup.hasFailures){
            failureReporting?.invoke(lookup)
        }
    }
}

inline fun <reified T: Any> KProperty1<Any, T>.getBrutForced(
    receiver: Any,
    noinline failureReporting: ((PropertyLookup)-> Unit)? = null
):T = getBrutForced(T::class, receiver, failureReporting)


fun <T: Any,  V: Any> KProperty1<T, V>.getBrutForced(
    returnClass: ClassAware<V>,
    receiver: T,
    failureReporting: ( (PropertyLookup)-> Unit)? = null
):V = getBrutForced(returnClass.kClass, receiver, failureReporting)


fun <T: Any> KProperty1<out Any, *>.returnClassOrNull(
    typeToken: TypeToken<T>
): KClass<T>?{
    val returnClass = returnType.classifier as? KClass<*> ?: return null
    return returnClass.safeClassCast(typeToken)
}
