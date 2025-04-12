package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import po.lognotify.classes.notification.Notifier
import po.lognotify.exceptions.ExceptionHandled
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.exceptions.ExceptionThrower
import po.lognotify.exceptions.ExceptionsThrown
import kotlin.coroutines.CoroutineContext


class TaskHandler<R>(
    private val task : TaskSealedBase<R>,
    val exceptionThrower:  ExceptionsThrown = ExceptionThrower(task),
    val exceptionHandler: ExceptionHandled = ExceptionHandler(task),
): ExceptionsThrown by exceptionThrower, ExceptionHandled by exceptionHandler, ResultantTask  {

    val currentTaskContext = task.context

    override val startTime: Long = task.startTime
    override var endTime: Long = task.endTime
    override val qualifiedName: String = task.qualifiedName
    override val taskName: String = task.taskName
    override val nestingLevel: Int = task.nestingLevel

    val taskHierarchyList : List<ResultantTask> = task.registry.getAsResultantTaskList()
    override val notifier: Notifier = Notifier(task)

    fun echo(message: String){
        notifier.echo(message)
    }
    suspend fun  info(message: String){
       notifier.info(message)
    }
    suspend fun warn(message: String){
        notifier.warn(message)
    }


    inline fun <T, R2>  withTaskContext(receiver: T,  crossinline block : suspend T.() -> R2):R2{
        return runBlocking {
            withContext(currentTaskContext) {
                block.invoke(receiver)
            }
        }
    }

}