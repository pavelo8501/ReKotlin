package po.lognotify.exceptions

import po.lognotify.TaskProcessor
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
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
internal fun TaskProcessor.handleException(
    exception: Throwable,
    task: TaskBase<*, *>,
    snapshot: Map<String, Any?>?
): ManagedException {
    if (exception is ManagedException) {
        if(task is RootTask){
            task.dataProcessor.debug("${exception.name()} has reached root task", "handleException", task)
            val taskData = task.dataProcessor.error(exception)
            exception.throwSelf(taskData.emitter)
        }

        exception.setPropertySnapshot(snapshot)
        return   when (exception.handler) {
            HandlerType.SKIP_SELF, HandlerType.UNMANAGED, HandlerType.CANCEL_ALL -> {
                val taskData = task.dataProcessor.error(exception)
                exception.addHandlingData(taskData.emitter,ManagedException.ExceptionEvent.Rethrown, taskData.message)
            }

            HandlerType.GENERIC -> {
                val taskData = task.dataProcessor.error(exception)
                exception.addHandlingData(taskData.emitter,ManagedException.ExceptionEvent.Rethrown, taskData.message)
            }
        }
    } else {

        val managed = ManagedException(exception.message.toString(),null, exception)
            .setHandler(HandlerType.GENERIC, task)
            .addHandlingData(task, ManagedException.ExceptionEvent.Registered, exception.message.toString())
            .setPropertySnapshot(snapshot)
        val taskData = task.dataProcessor.error(managed)
        return managed
    }
}

