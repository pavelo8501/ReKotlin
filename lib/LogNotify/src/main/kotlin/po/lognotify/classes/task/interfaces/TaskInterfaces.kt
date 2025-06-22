package po.lognotify.classes.task.interfaces

import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.models.TaskDispatcher.UpdateType
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.coroutines.CoroutineInfo
import po.misc.time.MeasuredContext


interface TopTask<T, R: Any?>: MeasuredContext, ResultantTask<T, R> {
    val subTasksCount: Int
    val isComplete: Boolean
    val registry: TaskRegistry<T, R>
}


interface ResultantTask<T, R:Any?> : MeasuredContext{
    val key : TaskKey
    val handler: TaskHandler<R>
    val coroutineInfo : CoroutineInfo
    val config: TaskConfig
    val dataProcessor: LoggerDataProcessor
}


interface UpdatableTasks{

    fun notifyUpdate(handler: UpdateType, task: ResultantTask<*, *>)
}

interface HandledTask<R: Any?>{

}