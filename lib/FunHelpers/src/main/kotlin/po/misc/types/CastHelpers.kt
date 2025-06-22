package po.misc.types

import po.misc.data.helpers.emptyOnNull
import po.misc.exceptions.ManageableException
import po.misc.exceptions.ManagedException
import kotlin.reflect.KClass
import kotlin.reflect.KType
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
    message: String? = null,
    code: Enum<*>? = null
): T {
    if(this == null){
        throw ManageableException.build<ManagedException>("${message.emptyOnNull()}. Unable to cast null to ${T::class.simpleName}", code)
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            val effectiveMessage = "${message.emptyOnNull()}. Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}"
            throw ManageableException.build<ManagedException>(effectiveMessage, code)
        }
    }
}

inline fun <reified T: Any, reified E: ManagedException> Any?.castOrThrow(
    message: String? = null,
    code: Enum<*>? = null
): T {
    if(this == null){
        throw ManageableException.build<E>("${message.emptyOnNull()}. Unable to cast null to ${T::class.simpleName}", code)
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            val effectiveMessage = "${message.emptyOnNull()}. Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}"
            throw ManageableException.build<E>(effectiveMessage, code)
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
        val exception = ManageableException.build<E>(message ?: "Unable to cast to ${kClass.simpleName}", code, e)
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
        throw ManageableException.build<E>("Cannot cast null to ${BASE::class.simpleName}", code)
    }
    if (!BASE::class.java.isAssignableFrom(this::class.java)) {
        val effectiveMessage = message ?: "Cannot cast ${this::class.simpleName} to ${BASE::class.simpleName}"
        throw ManageableException.build<E>(effectiveMessage, code)
    }
    return this as BASE
}
