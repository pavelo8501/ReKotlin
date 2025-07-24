package po.misc.types

import po.misc.collections.takeFromMatch
import po.misc.context.CTX
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManaged
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedPayload
import java.time.LocalDateTime
import kotlin.reflect.KClass


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}


inline fun <reified T : Any> T?.getOrThrow(
    exceptionProvider:(String)-> Throwable
):T {
    if (this != null) {
        return this
    } else {
        val message = "Expected class ${T::class.simpleName} is null"
        throw exceptionProvider(message)
    }
}


fun <T : Any> T?.getOrThrow(
    kClass: KClass<*>,
    exceptionProvider:(String)-> Throwable
):T {
    if(this != null){
        return this
    }else{
        val message = "Expected class ${kClass.simpleName} is null"
        throw exceptionProvider(message)
    }
}

internal fun currentCallerTrace(methodName: String): List<StackTraceElement> {
    return Thread.currentThread().stackTrace
        .takeFromMatch({ it.methodName == methodName }, 2)
}

@Deprecated("Inefficient", ReplaceWith("getOrManaged(callingContext: Any)"), DeprecationLevel.WARNING)
fun <T : Any> T?.getOrManaged(
    payload: ExceptionPayload
):T {
    if(this == null){
        payload.addDescription("getOrManaged returned null")
        throw ManagedException(payload)
    }else{
        return this
    }
}


fun <T: Any> T?.getOrManaged(
   callingContext: Any
):T {
    if(this == null){
        val trace = currentCallerTrace("getOrManaged")
        val message = "Result is null"
        val payload =  when(callingContext){
            is CTX -> { ManagedPayload(callingContext, message, trace) }
            else -> { ManagedPayload(callingContext::class.qualifiedName.toString(), message, trace) }
        }
        throw ManagedException(payload)
    }else{
        return this
    }
}

fun <T: Any> T?.getOrManaged(
    callingContext: Any,
    exceptionProvider: (ManagedCallSitePayload)-> Throwable,
):T {
    if(this == null){
        val trace = currentCallerTrace("getOrManaged")
        val message = "Result is null"
        val payload =  when(callingContext){
            is CTX -> { ManagedPayload(callingContext, message, trace) }
            else -> { ManagedPayload(callingContext::class.qualifiedName.toString(), message, trace) }
        }
        throw exceptionProvider.invoke(payload)
    }else{
        return this
    }
}


fun <T : Any> T?.getOrManaged(
    className: String,
):T {
    if(this != null){
        return this
    }else{
        val message = "Unable to return object of class: $className. Object is null."
        throwManaged(message)
    }
}

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