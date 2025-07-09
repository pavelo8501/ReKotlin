package po.misc.types

import po.misc.data.helpers.textIfNull
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.managedException
import po.misc.exceptions.throwManaged
import po.misc.interfaces.IdentifiableContext
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
        var message = "${payload.message}. getOrManaged returned null. ${payload.targetObject.textIfNull(""){ "Target object: ${it}"}}"
        message += payload.description

        val managed = payload.ctx.managedException(message, payload.source, payload.cause)
        payload.outputOverride?.invoke(managed)?: run {  throwManaged(message, payload.handler, payload.source, payload.cause)}
       throw managed
    }else{
        return this
    }
}


fun <T : Any> T?.getOrManaged(
    className: String,
    ctx: IdentifiableContext? = null
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