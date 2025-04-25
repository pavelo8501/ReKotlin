package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.task.models.TaskData
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.models.TaskKey
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

class TaskHandler<R: Any?>(
    val task : TaskSealedBase<R>,
    val taskData: TaskData,
    val exceptionHandler: ExceptionHandler<R>
):HandledTask<R>{


    override val key: TaskKey = taskData.taskKey
    override val notifier: Notifier get() = task.notifier

    fun echo(message: String){
        notifier.echo(message)
    }
    suspend fun info(message: String){
       notifier.info(message)
    }
    suspend fun warn(message: String){
        notifier.warn(message)
    }

    suspend fun hierarchyRoot(): RootTask<*>{
        return if(task is ManagedTask<*>){
            task.hierarchyRoot
        }else{
            task as RootTask
        }
    }

    override suspend fun handleFailure(vararg  handlers: HandlerType, fallbackFn: suspend (exception: ManagedException)->R){
        exceptionHandler.provideHandlerFn(handlers.toSet() , fallbackFn)
    }

    inline fun <T, R2>  withTaskContext(receiver: T,  crossinline block : suspend T.() -> R2):R2{
        var result: R2? = null
        var exception: Throwable? = null

        runBlocking {
            val job = launch(start = CoroutineStart.UNDISPATCHED, context = task.coroutineContext) {
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

//    suspend inline fun <T, R2> T.runManagedJob(crossinline  block: suspend T.()->R2){
//       val managedJob = ManagedJob(task)
//       managedJob.startJob(this@TaskHandler, this@runManagedJob, block)
//    }
//
//

}