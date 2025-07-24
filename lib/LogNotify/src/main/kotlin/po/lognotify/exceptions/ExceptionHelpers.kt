package po.lognotify.exceptions

import po.lognotify.TasksManaged
import po.lognotify.notification.models.ErrorSnapshot
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.enums.SeverityLevel
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.TaskBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.models.ExceptionData2
import po.misc.exceptions.toPayload
import po.misc.collections.selectUntil


@PublishedApi
internal fun <T: TasksManaged, R: Any?> handleException(
    exception: Throwable,
    container: RunnableContainer<T, R>,
    snapshot: List<PropertyData>?
): ManagedException {

    if (exception is ManagedException) {
        exception.setPropertySnapshot(snapshot)
        return   when (exception.handler) {
            HandlerType.Undefined -> {
                val exceptionHandler = container.effectiveTask.config.exceptionHandler
                exception.setHandler(exceptionHandler, container.source)
                container.effectiveTask.dataProcessor.error(exception)
                exception
            }

            HandlerType.SkipSelf -> {
                if (container.isRoot) {
                    val message = "Exception reached top. escalating"
                    container.effectiveTask.dataProcessor.notify(message, SeverityLevel.EXCEPTION)
                    val exceptionData =
                        ExceptionData2(ManagedException.ExceptionEvent.Executed, message, container.source)
                    throw exception.addExceptionData(exceptionData)
                } else {
                    val message = "Rethrowing"
                    container.effectiveTask.dataProcessor.warn(exception, message)
                    val exceptionData =
                        ExceptionData2(ManagedException.ExceptionEvent.Rethrown, message, container.source)
                    return exception.addExceptionData(exceptionData)
                }
            }
            HandlerType.CancelAll ->{
                val message = "Reached RootTask<${container.effectiveTask}>"
                val data =  ExceptionData2(ManagedException.ExceptionEvent.Executed,message,   container.source)
                exception.addExceptionData(data)
            }
        }
    } else {
        val exceptionHandler = container.effectiveTask.config.exceptionHandler
        val payload = container.source.toPayload{
            handler = exceptionHandler
        }
        val managed = ManagedException(payload)
        managed.setPropertySnapshot(snapshot)
        container.effectiveTask.dataProcessor.error(managed)
        return managed
    }
}


fun TaskBase<*, *>.provideBackTrace(): ErrorSnapshot{
    val compiledReport = createTaskData{
        val selectedSpans = actionSpans.selectUntil {
            (it.executionStatus == ExecutionStatus.Failing) || (it.executionStatus == ExecutionStatus.Faulty)
        }
        selectedSpans.map { it.createData() }
    }
    return compiledReport
}


fun <T: TasksManaged, R: Any?> RunnableContainer<T, R>.provideBackTrace(): ErrorSnapshot{
   val compiledReport = effectiveTask.createTaskData(){
       val selectedSpans = actionSpans.selectUntil {
           (it.executionStatus == ExecutionStatus.Failing) || (it.executionStatus == ExecutionStatus.Faulty)
       }
       selectedSpans.map { it.createData() }
    }
    return compiledReport
}

