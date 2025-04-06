package po.managedtask.classes.task

import po.managedtask.classes.notification.NotificationProvider
import po.managedtask.classes.notification.Notifier
import po.managedtask.exceptions.ExceptionHandled
import po.managedtask.exceptions.ExceptionHandler
import po.managedtask.exceptions.ExceptionThrower
import po.managedtask.exceptions.ExceptionsThrown

class TaskHandler(
    private val task : TaskSealedBase<*>,
    val notifier: NotificationProvider = Notifier(task),
    val thrower:  ExceptionsThrown = ExceptionThrower(task),
    val handler: ExceptionHandled = ExceptionHandler(task),
): NotificationProvider by notifier,  ExceptionsThrown by thrower, ExceptionHandled by handler, ResultantTask  {

    override val taskName: String = task.taskName

    val taskHierarchyList : List<ResultantTask> = task.registry.getAsResultantTaskList()

}