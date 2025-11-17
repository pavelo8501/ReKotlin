package po.misc.callbacks.validator

import po.misc.functions.LambdaType

interface ValidationProvider<T: Any> {
    fun validate(data: T): Boolean
    suspend fun validate(data: T, suspending: LambdaType.Suspended): Boolean
}