package po.lognotify.classes.task

import po.lognotify.classes.notification.Notifier
import po.lognotify.exceptions.ExceptionHandled
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.exceptions.ExceptionThrower
import po.lognotify.exceptions.ExceptionsThrown


class TaskHandler(
    private val task : TaskSealedBase<*>,
    val exceptionThrower:  ExceptionsThrown = ExceptionThrower(task),
    val exceptionHandler: ExceptionHandled = ExceptionHandler(task),
): ExceptionsThrown by exceptionThrower, ExceptionHandled by exceptionHandler, ResultantTask  {

    override val taskName: String = task.taskName
    override val nestingLevel: Int = task.nestingLevel

    val taskHierarchyList : List<ResultantTask> = task.registry.getAsResultantTaskList()
    private val notifier: Notifier = Notifier(task)

    suspend fun echo(message: String){
        notifier.echo(message)
    }

    suspend fun  info(message: String){
       notifier.info(message)
    }

    suspend fun warn(message: String){
        notifier.warn(this, message)
    }
}