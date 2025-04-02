package po.exposify.extensions

import kotlin.reflect.KClass

inline fun <reified T> Any.safeCast(): T? {
    return this as? T
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