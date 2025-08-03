package po.misc.functions.hooks.models

import po.misc.functions.hooks.ErrorPayload

data class ErrorData<T: Any>(
    val throwingObject:T,
    override val throwable: Throwable,
    override val snapshot: String
): ErrorPayload<T>
