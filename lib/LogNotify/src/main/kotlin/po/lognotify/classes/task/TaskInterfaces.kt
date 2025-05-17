package po.lognotify.classes.task

import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.SubNotifier
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskKey
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.time.ExecutionTimeStamp
import kotlin.coroutines.CoroutineContext


interface UpdatableTasks{
    companion object UpdateHandlers {
        val OnTaskCreated : Int = 1
        val OnTaskComplete : Int = 2
    }
    fun onTaskCreated(handler: UpdateType, callback: (TaskDispatcher.LoggerStats)-> Unit)
    fun onTaskComplete(handler: UpdateType, callback: (TaskDispatcher.LoggerStats)-> Unit)

    fun notifyUpdate(handler: UpdateType)
}







interface HandledTask<R: Any?>{
    val key: TaskKey
    val notifier: NotifierBase

    suspend fun handleFailure(vararg  handlers: HandlerType, fallbackFn: suspend (exception: ManagedException)->R)
}