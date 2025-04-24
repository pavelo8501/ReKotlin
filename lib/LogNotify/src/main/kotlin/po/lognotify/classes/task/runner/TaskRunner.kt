package po.lognotify.classes.task.runner

import kotlinx.coroutines.withContext
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.ControlledTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException


class TaskRunner<R: Any?>(
    val task: ControlledTask,
    val taskHandler: TaskHandler<R>
) {
    val exceptionHandler: ExceptionHandler<R> = ExceptionHandler(task)
    val notifier get() = task.notifier

    var startTime: Long = System.nanoTime()
    var endTime: Long = 0L
    private var elapsed: Float = 0.0F

    fun stopTimer(): Float {
        endTime = System.nanoTime()
        elapsed = (endTime - startTime) / 1_000_000f
        return elapsed
    }
    fun startTimer() {
        startTime = System.nanoTime()
    }


    suspend fun executedWithResult(value:R, handlerBlock: suspend TaskRunnerCallbacks<R>.()->Unit){
        stopTimer()
        val callbacks = TaskRunnerCallbacks<R>()
        handlerBlock.invoke(callbacks)
        callbacks.onResultFn?.invoke(value, elapsed)
    }
    suspend fun executedWithException(exception: ManagedException, handlerBlock: suspend TaskRunnerCallbacks<R>.()->Unit){
        stopTimer()
        val callbacks = TaskRunnerCallbacks<R>()
        handlerBlock.invoke(callbacks)
        callbacks.onUnhandledFn?.invoke(exception, elapsed)
    }

    suspend fun handleException(throwable: Throwable): ExceptionHandler.HandlerResult<R> {
        notifier.systemInfo(EventType.EXCEPTION_THROWN, SeverityLevel.WARNING, "Handling exception in ${task.taskName}. Message: ${throwable.message}")

        val managedException =
            throwable as? ManagedException ?: ManagedException(
                throwable.message.toString(),
            ).setSourceException(throwable).setHandler(HandlerType.GENERIC)

        when (managedException.handler) {
            HandlerType.CANCEL_ALL -> {

               if(exceptionHandler.isHandlerPresent(managedException.handler)){
                   val result = exceptionHandler.handleManaged(managedException)
                   return result
               }else{
                   notifier.systemInfo(EventType.EXCEPTION_HANDLED, SeverityLevel.WARNING, "Exception handled in ${task.taskName}")
                   task.notifyRootCancellation(managedException)
                   throw managedException
               }

                return exceptionHandler.handleManaged(managedException)
//                notifier.systemInfo(EventType.EXCEPTION_HANDLED, SeverityLevel.WARNING, "Exception handled in ${task.taskName}")
//                task.notifyRootCancellation(managedException)
//                return ExceptionHandler.HandlerResult<R>(null, managedException)
            }

            HandlerType.SKIP_SELF -> {
                if(exceptionHandler.isHandlerPresent(managedException.handler)){
                    val result = exceptionHandler.handleManaged(managedException)
                    return result
                }else{
                    return  ExceptionHandler.HandlerResult<R>(null, managedException)
                }
            }

            HandlerType.UNMANAGED -> {
                notifier.systemInfo(EventType.EXCEPTION_UNHANDLED, SeverityLevel.EXCEPTION, "Exception handling failure in ${task.taskName}")
                return ExceptionHandler.HandlerResult<R>(null, managedException)
            }
            else -> {
                return exceptionHandler.handleManaged(managedException)
            }
        }
    }
    suspend fun <T> execute(receiver:T,  block: suspend T.() -> R, handlerBlock: suspend TaskRunnerCallbacks<R>.()->Unit) {
        try{
            startTimer()
           val result = block.invoke(receiver)
            executedWithResult(result, handlerBlock)
        }catch (throwable: Throwable){
           val result = handleException(throwable)
           if(result.value != null){
               executedWithResult(result.value, handlerBlock)
           }else{
               executedWithException(result.exception!!, handlerBlock)
           }
        }
    }
    @PublishedApi
    internal suspend fun <T> execute(receiver:T,  block: suspend T.(TaskHandler<R>) -> R, handlerBlock: suspend TaskRunnerCallbacks<R>.()->Unit) {
        try {
            startTimer()
            val result =  block.invoke(receiver, taskHandler)
            executedWithResult(result, handlerBlock)
        }catch (throwable: Throwable){
            val result =  handleException(throwable)
            if(result.value != null){
                executedWithResult(result.value, handlerBlock)
            }else{
                executedWithException(result.exception!!, handlerBlock)
            }
        }
    }

    suspend inline fun <T, R2> executeJob(receiver:T, crossinline  block: suspend T.()->R2) {
        try {
            withContext(task.coroutineContext){
                val result = block.invoke(receiver)
            }
        }catch (throwable: Throwable){
            val result =  handleException(throwable)
            if(result.value != null){

            }else{

            }
        }
    }

}