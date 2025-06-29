package po.lognotify.exceptions

import po.lognotify.TaskProcessor
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.Task
import po.lognotify.classes.task.TaskBase
import po.lognotify.process.ProcessableContext
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.name
import po.misc.exceptions.toInfoString
import po.misc.exceptions.waypointInfo
import po.misc.interfaces.IdentifiableContext
import po.misc.types.castOrThrow
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties


@PublishedApi
internal fun handleException(
    exception: Throwable,
    task: TaskBase<*, *>,
    snapshot: Map<String, Any?>?,
    action: InlineAction? = null
): ManagedException {
    if (exception is ManagedException) {
        exception.setPropertySnapshot(snapshot)
        return   when (exception.handler) {
            HandlerType.SkipSelf ->{
                val taskData = task.dataProcessor.error(exception)
                exception.addHandlingData(taskData.emitter,ManagedException.ExceptionEvent.Rethrown, taskData.message)
            }
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
            HandlerType.CancelAll ->{
                if(task is RootTask){
                    exception.addHandlingData(task,  ManagedException.ExceptionEvent.Thrown)
                }
                exception
            }
        }
    } else {
        val exceptionHandler = task.config.exceptionHandler
        val managed = ManagedException(exception.message.toString(),null, exception)
            .setHandler(exceptionHandler, task)
            .addHandlingData(task, ManagedException.ExceptionEvent.Registered, exception.message.toString())
            .setPropertySnapshot(snapshot)
        task.dataProcessor.error(managed)
        return managed
    }
}

