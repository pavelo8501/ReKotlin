package po.lognotify.exceptions

import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.result.toTaskResult
import po.lognotify.enums.SeverityLevel
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.toInfoString
import po.misc.types.castOrThrow
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties


fun <T, R> Throwable.handleException(
    receiver: T,
    task: ResultantTask<R>,
): TaskResult<R> {

    val handler: ExceptionHandler<R> = task.exceptionHandler
    val snapshot = with(task) {
        takePropertySnapshot(receiver)
    }

    if (this is ManagedException) {
        setPropertySnapshot(snapshot)
        when (this.handler) {
            HandlerType.CANCEL_ALL -> return handler.handleManaged(this)

            HandlerType.SKIP_SELF -> return handler.handleManaged(this)

            HandlerType.UNMANAGED -> {
                task.notifier.systemInfo(
                    EventType.EXCEPTION_UNHANDLED,
                    SeverityLevel.EXCEPTION,
                    "Exception handling failure in ${task.key.taskName}"
                )
                return this.toTaskResult(task)
            }
            else -> return handler.handleManaged(this)
        }
    } else {
        val managed = ManagedException(this.toInfoString()).setSourceException(this)
            .setHandler(HandlerType.GENERIC)
            .setPropertySnapshot(snapshot)
        return handler.handleManaged(managed)
    }
}


context(task: ResultantTask<R>)
fun <T, R> takePropertySnapshot(receiver:T): MutableMap<String, Any?>? {
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
        task.notifier.warn(snapshot.map { "${it.key} : ${it.value}"}.joinToString(",") )
        return snapshot
    }
    return null
}
