package po.lognotify.classes.task

import po.lognotify.classes.notification.NotificationProvider
import po.lognotify.classes.task.models.CoroutineInfo
import po.lognotify.exceptions.SelfThrownException
import po.lognotify.models.TaskKey

interface ControlledTask  {
    val parent: TaskSealedBase<*>
    val key : TaskKey
    fun  propagateToParent(th: Throwable)

}

interface ResultantTask{
    val taskName: String
    val nestingLevel: Int
    val moduleName: String
    val qualifiedName: String
    val startTime: Long
    var endTime : Long
    val notifier: NotificationProvider

    val coroutineInfo : List<CoroutineInfo>


}