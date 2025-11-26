package po.misc.reflection

import po.misc.types.ClassAware
import po.misc.types.castOrThrow
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.IllegalCallableAccessException



fun <T: Any> KProperty1<Any, T>.getBrutForced(
    returnClass: KClass<T>,
    receiver: Any,
    failureReporting: ((PropertyLookup)-> Unit)? = null
) : T {
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
    noinline failureReporting: ( (PropertyLookup)-> Unit)? = null
):T = getBrutForced(T::class, receiver, failureReporting)

fun <T: Any> KProperty1<Any, T>.getBrutForced(
    returnClass: ClassAware<T>,
    receiver: Any,
    failureReporting: ( (PropertyLookup)-> Unit)? = null
):T = getBrutForced(returnClass.kClass, receiver, failureReporting)