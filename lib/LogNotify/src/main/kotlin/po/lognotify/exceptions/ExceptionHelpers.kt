package po.lognotify.exceptions

import po.lognotify.classes.action.ActionSpan
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.text


@PublishedApi
internal fun handleException(
    exception: Throwable,
    task: TaskBase<*, *>,
    snapshot: Map<String, Any?>?,
    action: ActionSpan<*>? = null
): ManagedException {
    if (exception is ManagedException) {
        exception.setPropertySnapshot(snapshot)
        return   when (exception.handler) {
            HandlerType.Undefined ->{
                val exceptionHandler = task.config.exceptionHandler
                if(action != null){
                    exception.setHandler(exceptionHandler, action)
                }else{
                    exception.setHandler(exceptionHandler, task)
                }
                task.dataProcessor.error(exception)
                exception
            }
            HandlerType.SkipSelf ->{
                val taskData = task.dataProcessor.error(exception)
                exception.addHandlingData(taskData.emitter,ManagedException.ExceptionEvent.Rethrown, taskData.message)
            }
            HandlerType.CancelAll ->{
                if(task is RootTask){
                    exception.addHandlingData(task,  ManagedException.ExceptionEvent.Thrown)
                }
                exception
            }
        }
    } else {
        val exceptionHandler = task.config.exceptionHandler
        val managed = ManagedException(exception.text(), null, exception)
            .setHandler(exceptionHandler, action?:task)
            .setPropertySnapshot(snapshot)
        task.dataProcessor.error(managed)
        return managed
    }
}

