package po.misc.types

import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.misc.exceptions.throwableToText
import kotlin.reflect.KClass
import kotlin.reflect.cast


inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

fun <T: Any> Any.safeCast(
    kClass: KClass<T>
):T? {
    return try {
        kClass.cast(this)
    } catch (e: ClassCastException) {
        null
    }
}

inline fun <reified BASE : Any> Any?.safeBaseCast(): BASE? {
    return when {
        this == null -> null
        BASE::class.java.isAssignableFrom(this::class.java) -> this as BASE
        else -> null
    }
}




fun <T: Any> Any?.castOrManaged(
    kClass: KClass<T>,
    callingContext: Any,
):T {

    val methodName = "castOrManaged"
    var message = "Cast to ${kClass.simpleName} failed."
    if(this == null){
        message += "Source object is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw  ManagedException(payload)
    }else{
        val operation = "Casting ${this::class} to ${kClass.simpleName}"
        return  try {
            kClass.cast(this)
        } catch (e: ClassCastException) {
            val payload = ManagedPayload("$operation ${e.throwableToText()}", methodName, callingContext)
            throw ManagedException(payload.setCause(e))
        }
    }
}

inline fun <reified T: Any> Any?.castOrManaged(
    callingContext: Any,
): T  = castOrManaged(T::class, callingContext)


fun <T: Any> Any?.castOrThrow(
    kClass: KClass<T>,
    callingContext: Any,
    exceptionProvider: (ManagedCallSitePayload)-> Throwable,
): T {
    val methodName = "castOrThrow"
    if (this == null) {
        val message = "Cast to ${kClass.simpleName} failed. Source object is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw exceptionProvider(payload)
    }
   return try {
        kClass.cast(this)
    } catch (e: ClassCastException) {
        val payload = ManagedPayload(e.throwableToText(), methodName, callingContext)
        throw exceptionProvider(payload.setCause(e))
    }
}

inline fun <reified T: Any> Any?.castOrThrow(
    callingContext: Any,
    noinline exceptionProvider: (ManagedCallSitePayload)-> Throwable,
): T = castOrThrow(T::class, callingContext, exceptionProvider)
