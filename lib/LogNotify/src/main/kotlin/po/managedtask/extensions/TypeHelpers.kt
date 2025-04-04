package po.managedtask.extensions

import po.managedtask.exceptions.ManagedExceptionBase

inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

fun <T: Any> T?.getOrThrow(ex : ManagedExceptionBase): T{
    return this ?: throw ex
}