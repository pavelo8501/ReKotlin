package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}


inline fun <reified T : Any, reified E: ManagedException> T?.getOrThrow(
    message: String? = null,
    code: Int = 0
): T {
    if(this == null){
        val effectiveMessage  = message?:"Expected class ${T::class.simpleName} is null"
        throw SelfThrownException.build<E>(effectiveMessage, code)
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
