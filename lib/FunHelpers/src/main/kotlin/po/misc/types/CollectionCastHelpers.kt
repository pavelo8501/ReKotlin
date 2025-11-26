package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.exceptions.ThrowableCallSitePayload
import po.misc.exceptions.ManagedPayload
import po.misc.exceptions.throwableToText
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

fun <T : Any> List<*>.castListSafe(
    kClass: KClass<T>
): List<T> {
    return this.mapNotNull{
        it?.safeCast(kClass)
    }
}

inline fun <reified T : Any> List<*>.castListSafe(): List<T> {
    return this.mapNotNull{
        it?.safeCast()
    }
}


fun <T : Any> List<*>.castListOrThrow(
    callingContext: Any,
    kClass: KClass<T>,
    exceptionProvider: (ThrowableCallSitePayload)-> Throwable,
): List<T> {
    return this.map{ it.castOrThrow(callingContext, kClass, exceptionProvider) }
}

inline fun <reified T : Any> List<*>.castListOrThrow(
    callingContext: Any,
    noinline exceptionProvider: (ThrowableCallSitePayload)-> Throwable,
): List<T> {
    return this.map{ it.castOrThrow(callingContext, T::class, exceptionProvider) }
}

fun <T : Any> List<*>.castListOrManaged(
    callingContext: Any,
    kClass: KClass<T>,
): List<T> {
    return this.map{ it.castOrManaged(callingContext, kClass) }
}

inline fun <reified T : Any> List<*>.castListOrManaged(
    callingContext: Any,
): List<T> = castListOrManaged(callingContext, T::class)


fun <BASE : Any> Any?.castBaseOrManaged(
    callingContext: Any,
    kClass : KClass<BASE>,
): BASE {
    val methodName = "castBaseOrManaged"
    var message = "Cast to ${kClass.simpleName} failed."
    if(this == null){
        message += "Source object is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw ManagedException(payload)
    }else{
        try {
            val  casted = kClass.cast(this)
            return casted
        }catch (ex: Throwable){
            val payload = ManagedPayload(ex.throwableToText(), methodName, callingContext)
            throw ManagedException(payload)
        }
    }
}


fun <BASE : Any> Any?.castBaseOrThrow(
    kClass : KClass<BASE>,
    callingContext: Any,
    exceptionProvider: (payload: ThrowableCallSitePayload)-> Throwable,
): BASE {
    val methodName = "castBaseOrThrow"
    var message = "Cast to ${kClass.simpleName} failed."
    if(this == null){
        message += "Source object is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw exceptionProvider.invoke(payload)
    }else{
        try {
            val  casted = kClass.cast(this)
            return casted
        }catch (ex: Throwable){
            val payload = ManagedPayload(ex.throwableToText(), methodName, callingContext)
           throw exceptionProvider.invoke(payload)
        }
    }
}

inline fun <reified BASE : Any> Any?.castBaseOrThrow(
    callingContext: Any,
    noinline exceptionProvider: (ThrowableCallSitePayload)-> Throwable,
): BASE = castBaseOrThrow(BASE::class, callingContext, exceptionProvider)



inline fun <reified T> Iterable<T>.equalOrThrow(size: Int, exceptionProvider:()-> Throwable):Iterable<T>{
    if(this.count() != size){
        throw exceptionProvider()
    }else{
        return this
    }
}

