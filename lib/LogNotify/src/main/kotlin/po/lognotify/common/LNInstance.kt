package po.lognotify.common

import po.lognotify.common.configuration.TaskConfig
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.models.LogData
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.misc.context.CTX
import po.misc.exceptions.ManagedException

interface LNInstance<T : CTX> : CTX {
    val receiver: T
    val header: String
    val config: TaskConfig
    val executionStatus: ExecutionStatus
    val nestingLevel: Int
    val rootTask: RootTask<*, *>
    val dataProcessor: LoggerDataProcessor

    fun changeStatus(status: ExecutionStatus)
    fun complete(): LNInstance<*>
    fun complete(exception: ManagedException)
}
