package po.misc.types

import po.misc.data.helpers.emptyOnNull
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManaged
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.TypedContext
import kotlin.reflect.KClass
import kotlin.reflect.cast


inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

inline fun <reified T: Any> Any.castOrFail(onFailure:(th: Throwable)-> Unit): T? {
   return try {
        this as T
    }catch (th: Throwable){
        onFailure.invoke(th)
        null
    }
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

fun <T: Any> Any.castOrFail(kClass: KClass<T>,  onFailure:(th: Throwable)-> Unit): T? {
    return try {
        kClass.cast(this)
    }catch (th: Throwable){
        onFailure.invoke(th)
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

fun <T: Any> Any?.castOrManaged(
    kClass: KClass<T>,
):T {
    return try {
        kClass.cast(this)
    } catch (e: ClassCastException) {
        val thisCtx = this
        val message = if(thisCtx != null){
            "Unable to cast ${thisCtx::class.simpleName.toString()} to ${kClass.simpleName}. ${e.message}"
        }else{
            "Unable to cast null to ${kClass.simpleName}"
        }
       throw ManagedException(message, null, e)
    }
}


inline fun <reified T: Any> Any?.castOrThrow(
    ctx: IdentifiableContext,
    exceptionProvider: (message: String)-> Throwable,
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
        if(exception is ManagedException){
            exception.throwSelf(ctx)
        }else{
            throw exception
        }
    }
}


inline fun <T: Any> Any?.castTypedOrThrow(
    ctx: TypedContext<T>,
    exceptionProvider: (message: String, original: Throwable)-> Throwable,
):T {
    return try {
        ctx.typeData.kClass.cast(this)
    } catch (e: ClassCastException) {
        val thisCtx = this
        val message = if(thisCtx != null){
            "Unable to cast ${thisCtx::class.simpleName.toString()} to ${ctx.typeData.kClass.simpleName}. ${e.message}"
        }else{
            "Unable to cast null to ${ctx.typeData.kClass.simpleName}"
        }
        val exception = exceptionProvider(message, e)
        if(exception is ManagedException){
            exception.throwSelf(ctx)
        }else{
            throw exception
        }
    }
}


inline fun <T: Any> Any?.castOrThrow(
    kClass: KClass<T>,
    exceptionProvider: (message: String, original: Throwable)-> Throwable,
):T {
    return try {
        kClass.cast(this)
    } catch (e: ClassCastException) {
        val thisCtx = this
        val message = if(thisCtx != null){
            "Unable to cast ${thisCtx::class.simpleName.toString()} to ${kClass.simpleName}. ${e.message}"
        }else{
            "Unable to cast null to ${kClass.simpleName}"
        }
        val exception = exceptionProvider(message, e)
        throw exception
    }
}
