package po.lognotify.classes.task

import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.task.runner.TaskRunner
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.misc.exceptions.CoroutineInfo
import po.misc.exceptions.ManagedException
import kotlin.coroutines.CoroutineContext


interface TaskIdentification {

    val taskName: String
    val nestingLevel: Int
    val moduleName: String
    val qualifiedName: String
    val startTime: Long
    var endTime : Long

}


interface ControlledTask : ResultantTask {
    val key : TaskKey
    val coroutineContext : CoroutineContext
    val notifier: Notifier
    val registry: TaskRegistry<*>
    val taskRunner: TaskRunner<*>
}

interface ResultantTask{

    val taskName: String
    val nestingLevel: Int
    val moduleName: String
    val qualifiedName: String
    val startTime: Long
    var endTime : Long


   suspend fun notifyRootCancellation(exception: ManagedException?)

    val coroutineInfo : List<CoroutineInfo>

}

interface HandledTask<R: Any?>{
    val key: TaskKey
    val coroutineContext : CoroutineContext
    val notifier: Notifier
    val taskRunner: TaskRunner<R>

    suspend fun notifyRootCancellation(exception: ManagedException?)
}