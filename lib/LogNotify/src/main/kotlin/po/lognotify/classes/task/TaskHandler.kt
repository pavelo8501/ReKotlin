package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.jobs.ManagedJob
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.task.runner.TaskRunner
import po.lognotify.extensions.castOrLoggerException
import po.misc.exceptions.CoroutineInfo
import po.misc.exceptions.HandlerType
import kotlin.coroutines.CoroutineContext

class TaskHandler<R>(
    val task : ControlledTask,
):ResultantTask  {

    val currentTaskContext: CoroutineContext = task.coroutineContext
    override val coroutineInfo: List<CoroutineInfo> = task.coroutineInfo

    override val startTime: Long = task.startTime
    override var endTime: Long = task.endTime
    override val qualifiedName: String = task.qualifiedName
    override val taskName: String = task.taskName
    override val moduleName: String = task.moduleName
    override val nestingLevel: Int = task.nestingLevel

    val notifier: Notifier = task.notifier
    val taskHierarchyList : List<ResultantTask> = task.registry.getAsResultantTaskList()


    fun echo(message: String){
        notifier.echo(message)
    }
    suspend fun info(message: String){
       notifier.info(message)
    }
    suspend fun warn(message: String){
        notifier.warn(message)
    }

    suspend fun setFallback(handler: HandlerType, fallbackFn: ()->R): TaskHandler<R>{
        val casted =  task.taskRunner.castOrLoggerException<TaskRunner<R>>()
        casted.exceptionHandler.provideHandlerFn(handler, fallbackFn)
        return this
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
        }
        return result as R2
    }

    suspend inline fun <T, R2> T.runManagedJob(crossinline  block: suspend T.()->R2){
        val managedJob = ManagedJob(task)
        managedJob.startJob(this@TaskHandler, this@runManagedJob, block)
    }

}