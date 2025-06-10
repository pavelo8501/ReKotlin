package po.lognotify.exceptions

import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.Task
import po.lognotify.classes.task.TaskBase
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.name
import po.misc.exceptions.toInfoString
import po.misc.exceptions.waypointInfo
import po.misc.types.castOrThrow
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties


fun <T, R> handleException(
    exception: Throwable,
    task: TaskBase<T, R>,
): ManagedException {
    val snapshot =  if(task is Task<*, *>){
        takePropertySnapshot(task, task.ctx)
    }else{
        null
    }
    if (exception is ManagedException) {
        if(task is RootTask){
            task.dataProcessor.debug("${exception.name()} has reached root task", "handleException", task)
            val taskData = task.dataProcessor.error(exception.waypointInfo(), task)
            exception.throwSelf(taskData.emitter)
        }

        exception.setPropertySnapshot(snapshot)
        return   when (exception.handler) {
            HandlerType.SKIP_SELF, HandlerType.UNMANAGED, HandlerType.CANCEL_ALL -> {
                val taskData = task.dataProcessor.error("Forwarded", task)
                exception.addHandlingData(taskData.emitter,ManagedException.ExceptionEvent.Rethrown, taskData.message)
            }

            HandlerType.GENERIC -> {
                val taskData = task.dataProcessor.error("Forwarded(${exception.message})", task)
                exception.addHandlingData(taskData.emitter,ManagedException.ExceptionEvent.Rethrown, taskData.message)
            }
        }
    } else {

        val taskData = task.dataProcessor.error("Registered ${exception.toInfoString()}", task)
        val managed = ManagedException(exception.message.toString(),null, exception)
            .setHandler(HandlerType.GENERIC,taskData.emitter)
            .addHandlingData(taskData.emitter,ManagedException.ExceptionEvent.Registered, exception.message.toString())
            .setPropertySnapshot(snapshot)
        return managed
    }
}


fun <T, R> takePropertySnapshot(task: TaskBase<T, R>,  receiver:T): MutableMap<String, Any?>? {
    val snapshot: MutableMap<String, Any?> = mutableMapOf()
    receiver?.let {
        it::class.memberProperties.filter { prop -> prop.findAnnotation<LogOnFault>() != null }
            .forEach { annotated ->
                val casted = annotated.castOrThrow<KProperty1<T, Any?>, LoggerException>()
                snapshot[annotated.name] = casted.get(receiver)
            }
        it::class.staticProperties.filter { prop -> prop.findAnnotation<LogOnFault>() != null }
            .forEach { annotated ->
                snapshot[annotated.name] = annotated.get()
            }
    }
    if (snapshot.values.count() > 0) {
        val snapshotText = snapshot.map { "${it.key} : ${it.value}"}.joinToString(",")
        task.dataProcessor.warn(snapshotText, task)
        return snapshot
    }
    return null
}
