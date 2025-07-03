package po.misc.types

import po.misc.data.helpers.emptyOnNull
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManaged
import po.misc.interfaces.IdentifiableContext
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


inline fun <reified T: Any> Any?.castOrManaged(
    payload: ManagedCallSitePayload
): T {
    if(this == null){
        payload.message = "Unable to cast null to ${T::class.simpleName}"
        throwManaged(payload)
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            payload.message = "Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}"
            throwManaged(payload)
        }
    }
}


inline fun <reified T: Any> Any?.castOrManaged(
    message: String? = null,
    handler: HandlerType = HandlerType.SkipSelf
): T {
    if(this == null){
        throwManaged("${message.emptyOnNull()}. Unable to cast null to ${T::class.simpleName}", handler)
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            val effectiveMessage = "${message.emptyOnNull()}. Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}"
            throwManaged(effectiveMessage, handler)
        }
    }
}

inline fun <reified T: Any, E: ManagedException> Any?.castOrThrow(
    ctx: IdentifiableContext? = null,
    exceptionProvider: (message: String)-> E,
): T {
    val result =  this as? T
    if(result != null){
        return result
    }else{
        val message =   if(this == null){
            "Unable to cast null to ${T::class.simpleName}"
        }else{
            "Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}"
        }
        val exception = exceptionProvider(message)
        if(ctx != null){
            exception.throwSelf(ctx)
        }else{
            throw exception
        }
    }
}

inline fun <T: Any, reified E: ManagedException> Any?.castOrThrow(
    kClass: KClass<T>,
    ctx: IdentifiableContext? = null,
    exceptionProvider: (message: String)-> E,
): T {
    return try {
        kClass.cast(this)
    } catch (e: ClassCastException) {
        val thisCtx = this
        val message = if(thisCtx != null){
            "Unable to cast ${thisCtx::class.simpleName.toString()} to ${kClass.simpleName}"
        }else{
            "Unable to cast null to ${kClass.simpleName}"
        }
        val exception = exceptionProvider(message)
        if(ctx != null){
            exception.throwSelf(ctx)
        }else{
            throw exception
        }
    }
}

inline fun <T : Any, reified E: ManagedException> List<*>.castListOrThrow(
    kClass: KClass<T>,
    ctx: IdentifiableContext? = null,
    exceptionProvider: (message: String)-> E,
): List<T> {
    return this.mapNotNull { it.castOrThrow<T, E>(kClass, ctx, exceptionProvider) }
}

inline fun <reified BASE : Any, reified E : ManagedException> Any?.castBaseOrThrow(
    ctx: IdentifiableContext? = null,
    exceptionProvider: (message: String)-> E,
): BASE {
    try {
        return this as BASE
    }catch (ex: Throwable){
        val message = if (this == null) {
            "Cannot cast null to ${BASE::class.simpleName}"
        }else{
            "Cannot cast ${this::class.simpleName} to ${BASE::class.simpleName}"
        }
        val exception = exceptionProvider(message)
        if(ctx != null){
            exception.throwSelf(ctx)
        }else{
            throw exception
        }
    }
}
