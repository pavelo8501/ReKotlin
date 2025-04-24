package po.misc

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}


inline fun <reified T: Any> getType(): KClass<T> {
    return T::class
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

inline fun <reified T: Any> T.getKType(): KType {
    return typeOf<T>()
}