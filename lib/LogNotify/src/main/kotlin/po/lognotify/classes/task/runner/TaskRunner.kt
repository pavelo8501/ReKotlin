package po.lognotify.classes.task.runner

import kotlinx.coroutines.withContext
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.ControlledTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.exceptions.LoggerException
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.types.castOrThrow
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties


class TaskRunner<R: Any?>(
    val task: ControlledTask,
    val taskHandler: TaskHandler<R>,
    val exceptionHandler: ExceptionHandler<R>,
) {
    val notifier: Notifier get() = task.notifier
    val taskName: String get()= task.key.taskName

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

    private fun <T> takePropertySnapshot(receiver:T): Map<String, Any?>{
        val snapshot : MutableMap<String, Any?> = mutableMapOf()
        receiver?.let {
            it::class.memberProperties.filter {prop-> prop.findAnnotation<LogOnFault>() != null }.forEach {annotated->
                val casted = annotated.castOrThrow<KProperty1<T, Any?>, LoggerException>()
                snapshot[annotated.name] = casted.get(receiver)
            }
            it::class.staticProperties.filter { prop-> prop.findAnnotation<LogOnFault>() != null }.forEach { annotated->
                snapshot[annotated.name] = annotated.get()
            }
        }
        return snapshot
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
        notifier.systemInfo(EventType.EXCEPTION_THROWN, SeverityLevel.WARNING, "Handling exception in ${taskName}. Message: ${throwable.message}")

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
                   notifier.systemInfo(EventType.EXCEPTION_HANDLED, SeverityLevel.WARNING, "Exception handled in ${taskName}")
                   task.notifyRootCancellation(managedException)
                   return exceptionHandler.handleManaged(managedException)
               }
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
                notifier.systemInfo(EventType.EXCEPTION_UNHANDLED, SeverityLevel.EXCEPTION, "Exception handling failure in ${taskName}")
                return ExceptionHandler.HandlerResult<R>(null, managedException)
            }
            else -> {
                return exceptionHandler.handleManaged(managedException)
            }
        }
    }


    @PublishedApi
    internal suspend fun <T> execute(receiver:T,  block: suspend T.(TaskHandler<R>) -> R, handlerBlock: suspend TaskRunnerCallbacks<R>.()->Unit) {

        val receiver = receiver
        try {
            startTimer()
            val result =  block.invoke(receiver, taskHandler)
            executedWithResult(result, handlerBlock)
        }catch (throwable: Throwable){
            val snapshot = takePropertySnapshot<T>(receiver)
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