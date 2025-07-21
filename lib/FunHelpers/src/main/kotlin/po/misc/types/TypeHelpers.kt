package po.misc.types

import po.misc.data.helpers.textIfNull
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.managedException
import po.misc.exceptions.throwManaged
import po.misc.context.Identifiable
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


fun <T : Any> T?.getOrManaged(
    payload: ManagedCallSitePayload
):T {
    if(this == null){
        payload.addDescription("getOrManaged returned null")
        throw ManagedException(payload.message, payload)
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

inline fun <reified T, reified U> initializeContexts(
    receiverInstance: T,
    paramInstance: U,
    block: T.(U) -> Unit
) {
    receiverInstance.block(paramInstance)
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