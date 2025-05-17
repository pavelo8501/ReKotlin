package po.lognotify.extensions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.RootSyncTaskHandler
import po.lognotify.classes.task.RootTaskSync
import po.lognotify.classes.task.TaskResultSync
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandlerSync

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.lognotify.exceptions.HandledResult
import po.lognotify.exceptions.toHandledResult


fun <R> R.toResult(task : RootTaskSync<R>):TaskResultSync<R>{
   return task.taskResult.provideResult(this)
}

fun<R> HandledResult<R>.toResult():TaskResultSync<R>{
    val result = TaskResultSync(this.task)
    if(this.success){
        result.provideResult(this.value)
    }else{
        result.provideThrowable(this.exception)
    }
    return result
}

fun <R> Throwable.handleException(
    task: RootTaskSync<R>,
    snapshot : Map<String, Any?>? = null
): HandledResult<R> {
   val handler: ExceptionHandlerSync<R> = task.exceptionHandler

    val managedException =
        this as? ManagedException ?: ManagedException(
            this.message.toString(),
        ).setSourceException(this).setHandler(HandlerType.GENERIC)
    managedException.snapshot = snapshot

    when (managedException.handler) {
        HandlerType.CANCEL_ALL -> {
            return handler.handleManaged(managedException)

        }
        HandlerType.SKIP_SELF -> {
            return handler.handleManaged(managedException)
        }
        HandlerType.UNMANAGED -> {
            task.notifier.systemInfo(EventType.EXCEPTION_UNHANDLED, SeverityLevel.EXCEPTION, "Exception handling failure in ${task.key.taskName}")
           return managedException.toHandledResult(task)
        }
        else -> {
           return handler.handleManaged(managedException)
        }
    }
}