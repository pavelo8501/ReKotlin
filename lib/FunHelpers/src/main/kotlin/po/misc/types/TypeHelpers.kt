package po.misc.types

import po.misc.collections.takeFromMatch
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedPayload
import po.misc.exceptions.extractCallSiteMeta
import java.time.LocalDateTime
import kotlin.reflect.KClass


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}


fun <T: Any> T?.getOrThrow(
    expectedClass: KClass<*>,
    callingContext: Any,
    exceptionProvider: (ManagedCallSitePayload)-> Throwable,
):T {
    val methodName = "getOrThrow"
    if(this == null){
        val message = "Expected: ${expectedClass.simpleName}. Result is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw exceptionProvider.invoke(payload)
    }else{
        return this
    }
}

inline fun <reified T> T?.getOrThrow(
    callingContext: Any,
    noinline exceptionProvider: (ManagedCallSitePayload)-> Throwable
):T = getOrThrow(T::class, callingContext, exceptionProvider)


@PublishedApi
@JvmName("getOrManagedImplementation")
internal fun <T> getOrManaged(
    receiver: T?,
    callingContext: Any,
    expectedClass: KClass<*>,
): T{
    val methodName = "getOrManaged"
    if(receiver == null){
        val message = "Expected: ${expectedClass.simpleName}. Result is null"
        val payload = ManagedPayload(message, methodName, callingContext)
        throw ManagedException(payload)
    }else{
        return receiver
    }
}

fun <T> T?.getOrManaged(
    callingContext: Any,
    expectedClass: KClass<*>,
):T = getOrManaged(this, callingContext,  expectedClass)


inline fun <reified T> T?.getOrManaged(
    callingContext: Any
):T = getOrManaged(this,  callingContext, T::class)


fun Any?.isNull(): Boolean{
    return this == null
}

fun Any?.isNotNull(): Boolean{
    return this != null
}

fun <T: Any> TypeData<T>.getDefaultForType(): T? {
    val result = when (this.kType.classifier) {
        Int::class -> -1
        String::class -> "Default"
        Boolean::class -> false
        Long::class -> -1L
        LocalDateTime::class -> {
            LocalDateTime.now()
        }
        else -> null
    }
    return result?.safeCast(this.kClass)
}


inline fun <T: Any> T?.letOrException(ex : ManagedException, block: (T)-> T){
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}

fun <T: Any?, E: ManagedException> T.testOrException( exception : E, predicate: (T) -> Boolean): T{
    if (predicate(this)){
        return this
    }else{
        throw exception
    }
}