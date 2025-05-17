package po.lognotify.classes.task.interfaces

import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskHandlerBase
import po.lognotify.models.CommonTaskRegistry
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.exceptions.ManagedException
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import kotlin.coroutines.CoroutineContext


interface TopTask<R: Any?>: MeasuredContext, ResultantTask {
    override val key : TaskKey
    val notifier : RootNotifier
    val subTasksCount: Int
    val isComplete: Boolean
    val registry: CommonTaskRegistry
}

interface ChildTask{
    val key : TaskKey
}

interface ResultantTask : MeasuredContext{
    val key : TaskKey
    val taskHandler: TaskHandlerBase<*>
}