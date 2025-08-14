package po.lognotify.tasks.interfaces

import po.lognotify.common.configuration.TaskConfig
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.tasks.TaskHandler
import po.misc.context.CTX
import po.misc.coroutines.CoroutineInfo
import po.misc.time.MeasuredContext

interface TopTask<T : CTX, R : Any?> :
    MeasuredContext,
    ResultantTask<T, R> {
    val subTasksCount: Int
    val isComplete: Boolean
    val registry: TaskRegistry<T, R>
}

interface ResultantTask<T, R : Any?> : MeasuredContext {
    val key: TaskKey
    val handler: TaskHandler<R>
    val coroutineInfo: CoroutineInfo
    val config: TaskConfig
    val dataProcessor: LoggerDataProcessor
}


interface HandledTask<R : Any?>
