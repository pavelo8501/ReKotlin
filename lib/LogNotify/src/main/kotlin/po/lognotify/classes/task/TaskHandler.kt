package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.task.models.CoroutineInfo
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

    val currentTaskContext: CoroutineContext = task.context
    val  handleException : suspend (Throwable) -> Throwable? = { task.handleException(it) }
    override val coroutineInfo: List<CoroutineInfo> = task.coroutineInfo

    override val startTime: Long = task.startTime
    override var endTime: Long = task.endTime
    override val qualifiedName: String = task.qualifiedName
    override val taskName: String = task.taskName
    override val moduleName: String = task.moduleName
    override val nestingLevel: Int = task.nestingLevel

    val taskHierarchyList : List<ResultantTask> = task.registry.getAsResultantTaskList()
    override val notifier: Notifier = Notifier(task)

    fun echo(message: String){
        notifier.echo(message)
    }

    suspend fun info(message: String){
       notifier.info(message)
    }

    suspend fun warn(message: String){
        notifier.warn(message)
    }

    inline fun <T, R2>  withTaskContext(receiver: T,  crossinline block : suspend T.() -> R2):R2{
        var result: R2? = null
        var exception: Throwable? = null

        runBlocking {
            val job = launch(start = CoroutineStart.UNDISPATCHED, context = currentTaskContext) {
                try {
                    result = block(receiver)
                } catch (e: Throwable) {
                    exception = e
                }
            }
            job.join()
            if(exception != null){
                handleException.invoke(exception)
            }
        }
        return result as R2
    }

}