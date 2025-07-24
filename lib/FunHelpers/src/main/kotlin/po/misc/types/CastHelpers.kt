package po.misc.types

import po.misc.data.helpers.emptyOnNull
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManaged
import po.misc.context.CTX
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
    ctx: CTX,
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
        throw exception
    }
}


inline fun <reified T: Any> Any?.castOrThrow(
    payload: ManagedCallSitePayload,
    exceptionProvider: (message: String)-> Throwable,
): T {
    if(this == null){
        val msg = "Unable to cast null to ${T::class.simpleName}"
        val exception =  exceptionProvider(msg)
        throw exception
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            val msg = "Unable to cast ${this::class.simpleName} to ${T::class.simpleName}"
            val exception =  exceptionProvider(msg)
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
