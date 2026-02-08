package po.misc.callbacks.validator

import po.misc.functions.Suspended

interface ValidationProvider<T> {
    fun validate(data: T): Boolean
    suspend fun validate(data: T, suspending: Suspended): Boolean
}