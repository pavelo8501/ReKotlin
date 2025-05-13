package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.companionObjectInstance


inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

inline fun <reified T: Any, reified E: ManagedException> Any?.castOrThrow(
    message: String? = null,
    code: Int = 0
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
    code: Int = 0
): T {
    return try {
        kClass.cast(this)
    } catch (e: ClassCastException) {
        val exception = SelfThrownException.build<E>(message ?: "Unable to cast to ${kClass.simpleName}", code)
        exception.setSourceException(e)
        throw exception
    }
}

inline fun <T : Any, reified E: ManagedException> List<*>.castListOrThrow(
    kClass: KClass<T>,
    message: String? = null,
    code: Int = 0
): List<T> {
    return this.mapNotNull { it.castOrThrow<T, E>(kClass, message, code) }
}

