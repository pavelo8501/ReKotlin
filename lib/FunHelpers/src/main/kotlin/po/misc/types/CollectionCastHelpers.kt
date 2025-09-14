package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedPayload
import po.misc.exceptions.throwableToText
import kotlin.reflect.KClass
import kotlin.reflect.full.cast


fun <T : Any> filterByType(
     typeData: Typed<T>,
     list:  List<Any>
): List<T> {
    return list.mapNotNull {it.safeCast(typeData.kClass) }
}


fun <T : Any>  List<Any>.findByTypeFirstOrNull(
    typeData: Typed<T>,
):T? {
    return firstNotNullOfOrNull { it.safeCast(typeData.kClass) }
}


inline fun <reified T : Any> List<*>.castListSafe(
    callingContext: Any
): List<T> {
    return this.mapNotNull{
        try {
            it.castOrManaged(callingContext)
        }catch (th: Throwable){
            null
        }

    }
}

fun <T : Any> List<*>.castListSafe(
    callingContext: Any,
    kClass: KClass<T>,
): List<T> {
    return this.mapNotNull{
        try {
            it.castOrManaged(callingContext, kClass)
        }catch (th: Throwable){
            null
        }

    }
}



fun <T : Any> List<*>.castListOrThrow(
    callingContext: Any,
    kClass: KClass<T>,
    exceptionProvider: (ManagedCallSitePayload)-> Throwable,
): List<T> {
    return this.map{ it.castOrThrow(kClass, callingContext,  exceptionProvider) }
}

inline fun <reified T : Any> List<*>.castListOrThrow(
    callingContext: Any,
    noinline exceptionProvider: (ManagedCallSitePayload)-> Throwable,
): List<T> {
    return this.map{ it.castOrThrow(T::class, callingContext,  exceptionProvider) }
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
    exceptionProvider: (payload: ManagedCallSitePayload)-> Throwable,
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
    noinline exceptionProvider: (ManagedCallSitePayload)-> Throwable,
): BASE = castBaseOrThrow(BASE::class, callingContext, exceptionProvider)



inline fun <reified T> Iterable<T>.countEqualsOrException(equalsTo: Int, exception:ManagedException):Iterable<T>{
    val actualCount = this.count()
    if(actualCount != equalsTo){
        throw exception
    }else{
        return this
    }
}

