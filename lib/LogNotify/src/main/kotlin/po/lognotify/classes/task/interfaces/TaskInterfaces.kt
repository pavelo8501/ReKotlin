package po.lognotify.classes.task.interfaces

import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.exceptions.CoroutineInfo
import po.misc.time.MeasuredContext
import po.misc.types.UpdateType


interface TopTask<R: Any?>: MeasuredContext, ResultantTask<R> {
    override val notifier : RootNotifier<R>
    val subTasksCount: Int
    val isComplete: Boolean
    val registry: TaskRegistry<R>
}


interface ResultantTask<R:Any?> : MeasuredContext{
    val key : TaskKey
    val handler: TaskHandler<R>
    val notifier : NotifierBase
    val exceptionHandler: ExceptionHandler<R>
    val coroutineInfo : CoroutineInfo
    val config: TaskConfig
}


interface UpdatableTasks{

    // fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats)-> Unit)
    // fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats)-> Unit)
    fun notifyUpdate(handler: UpdateType, task: ResultantTask<*>)
}

interface HandledTask<R: Any?>{
    val notifier: NotifierBase
    //fun handleFailure(vararg  handlers: HandlerType, fallbackFn: (exception: ManagedException)->R)
}