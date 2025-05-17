package po.lognotify.classes.task

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.SubNotifier
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.exceptions.ExceptionHandlerSync
import po.lognotify.models.TaskKey
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException


sealed class TaskHandlerBase<R: Any?>(){

    abstract val key: TaskKey
    abstract val notifier: NotifierBase

    fun echo(message: String){
        notifier.echo(message)
    }
    fun info(message: String){
        notifier.info(message)
    }
    fun warn(message: String){
        notifier.warn(message)
    }
    fun warn(th : Throwable, message: String) = notifier.warn(th, message)
}

class TaskHandler<R: Any?>(
    val task : TaskAsyncBase<R>,
    val exceptionHandler: ExceptionHandler<R>,
):TaskHandlerBase<R>(), HandledTask<R>{

    override val key: TaskKey get() = task.key
    override val notifier: NotifierBase get() = task.notifier

    override suspend fun handleFailure(vararg  handlers: HandlerType, fallbackFn: suspend (exception: ManagedException)->R){
        exceptionHandler.provideHandlerFn(handlers.toSet() , fallbackFn)
    }

    inline fun <T, R2>  withTaskContext(receiver: T,  crossinline block : suspend T.() -> R2):R2{
        var result: R2? = null
        runBlocking {
            val job = launch(start = CoroutineStart.UNDISPATCHED, context = task.coroutineContext) {
                result = block(receiver)
            }
            job.join()
        }
        return result as R2
    }
}


class RootSyncTaskHandler<R: Any?>(
    val task : RootTaskSync<R>,
    val exceptionHandler : ExceptionHandlerSync<R>
): TaskHandlerBase<R>(){

    override val notifier: RootNotifier = RootNotifier(task)
    override val key: TaskKey get() = task.key
}

class SyncTaskHandler<R: Any?>(
    val task : SubTaskSync<R>,
    val exceptionHandler : ExceptionHandlerSync<R>
): TaskHandlerBase<R>(){

    override val notifier: SubNotifier = SubNotifier(task, task.rootTask.notifier)
    override val key: TaskKey get() = task.key

}


