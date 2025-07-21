package po.lognotify.common

import po.lognotify.tasks.ExecutionStatus
import po.misc.context.CTX

interface LogInstance<T: CTX> : CTX {
    val receiver:T

    fun changeStatus(status:ExecutionStatus)

}