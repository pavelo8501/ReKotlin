package po.lognotify.classes.task

import po.lognotify.classes.notification.NotificationProvider
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.task.models.CoroutineInfo
import po.lognotify.classes.task.runner.TaskRunner
import po.lognotify.exceptions.SelfThrownException
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import kotlin.coroutines.CoroutineContext

interface ControlledTask : ResultantTask {
    val key : TaskKey
    val context : CoroutineContext
    val notifier: Notifier
    val registry: TaskRegistry<*>
    val taskRunner: TaskRunner<*>
    val taskHandler: TaskHandler<*>
}

interface ResultantTask{

    val taskName: String
    val nestingLevel: Int
    val moduleName: String
    val qualifiedName: String
    val startTime: Long
    var endTime : Long
   // val notifier: NotificationProvider

    val coroutineInfo : List<CoroutineInfo>


}