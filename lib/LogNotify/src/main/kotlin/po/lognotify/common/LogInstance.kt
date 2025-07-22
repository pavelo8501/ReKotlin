package po.lognotify.common

import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.models.TaskConfig
import po.misc.context.CTX

interface LogInstance<T: CTX> : CTX {
    val receiver:T
    val config: TaskConfig
    val executionStatus: ExecutionStatus

    fun changeStatus(status:ExecutionStatus)

}