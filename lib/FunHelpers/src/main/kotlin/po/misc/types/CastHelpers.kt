package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.companionObjectInstance


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

inline fun <reified T: Any, reified E: ManagedException> Any?.castOrThrow(
    message: String? = null,
    code: Enum<*>? = null
): T {
    if(this == null){
        throw SelfThrownException.build<E>("Unable to cast null to ${T::class.simpleName}", code)
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            val effectiveMessage = message?:"Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}"
            throw SelfThrownException.build<E>(effectiveMessage, code)
        }
    }
}

inline fun <T: Any, reified E: ManagedException> Any?.castOrThrow(
    kClass: KClass<T>,
    message: String? = null,
    code: Enum<*>
): T {
    return try {
        kClass.cast(this)
    } catch (e: ClassCastException) {
        val exception = SelfThrownException.build<E>(message ?: "Unable to cast to ${kClass.simpleName}", code, e)
        throw exception
    }
}

inline fun <T : Any, reified E: ManagedException> List<*>.castListOrThrow(
    kClass: KClass<T>,
    message: String? = null,
    code: Enum<*>
): List<T> {
    return this.mapNotNull { it.castOrThrow<T, E>(kClass, message, code) }
}

inline fun <reified BASE : Any, reified E : ManagedException> Any?.castBaseOrThrow(
    message: String? = null,
    code: Enum<*> ?  = null
): BASE {
    if (this == null) {
        throw SelfThrownException.build<E>("Cannot cast null to ${BASE::class.simpleName}", code)
    }
    if (!BASE::class.java.isAssignableFrom(this::class.java)) {
        val effectiveMessage = message ?: "Cannot cast ${this::class.simpleName} to ${BASE::class.simpleName}"
        throw SelfThrownException.build<E>(effectiveMessage, code)
    }
    return this as BASE
}
