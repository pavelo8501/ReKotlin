package po.lognotify.classes.task

import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.task.runner.TaskRunner
import po.lognotify.models.TaskDispatcher
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
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

interface TaskIdentification {

    val taskName: String
    val nestingLevel: Int
    val moduleName: String
    val qualifiedName: String
    val startTime: Long
    var endTime : Long
    val coroutineContext : CoroutineContext
}


interface ControlledTask : ResultantTask {
    val key : TaskKey
    val coroutineContext : CoroutineContext
    val notifier: Notifier
    val registry: TaskRegistry<*>
    val taskRunner: TaskRunner<*>
    override val taskData : TaskIdentification
}

interface ResultantTask{
    val taskData : TaskIdentification
    suspend fun notifyRootCancellation(exception: ManagedException?)
   // val coroutineInfo : List<CoroutineInfo>
}


interface HandledTask<R: Any?>{
    val key: TaskKey
    val notifier: Notifier

    suspend fun handleFailure(vararg  handlers: HandlerType, fallbackFn: suspend (exception: ManagedException)->R)
}