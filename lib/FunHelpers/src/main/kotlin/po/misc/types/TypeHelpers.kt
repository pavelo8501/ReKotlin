package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}

inline fun <reified T : Any, reified E: ManagedException> T?.getOrThrow(
    message: String? = null,
    code: Enum<*> ?  = null
):T {
    if(this == null){
        val effectiveMessage  = message?:"Expected class ${T::class.simpleName} is null"
        throw ManageableException.build<E>(effectiveMessage, code)
    }else{
        return this
    }
}


fun <T : Any> T?.getOrManaged(
    className: String
):T {
    if(this == null){
        val  code: Enum<*>? = null
        val message = "Unable to return object of class: $className. Object is null."
        throw ManageableException.build<ManagedException>(message, code)
    }else{
        return this
    }
}

inline fun <reified T, reified U> initializeContexts(
    receiverInstance: T,
    paramInstance: U,
    block: T.(U) -> Unit
) {
    receiverInstance.block(paramInstance)
}

inline fun <reified T: Any> T.getType(): KClass<T> {
    return T::class
}

fun Any?.isNull(): Boolean{
    return this == null
}

fun Any?.isNotNull(): Boolean{
    return this != null
}