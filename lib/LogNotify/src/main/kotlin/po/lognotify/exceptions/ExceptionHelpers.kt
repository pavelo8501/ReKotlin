package po.lognotify.exceptions

import po.lognotify.TasksManaged
import po.lognotify.notification.models.ErrorSnapshot
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.notification.error
import po.lognotify.notification.warning
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.TaskBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.models.ExceptionData
import po.misc.collections.selectUntil
import po.misc.exceptions.throwableToText
import po.misc.exceptions.toManaged


@PublishedApi
internal fun <T: TasksManaged, R: Any?> handleException(
    exception: Throwable,
    container: RunnableContainer<T, R>,
    snapshot: List<PropertyData>?
): ManagedException {
    fun firstOccurred(managed: ManagedException): ExceptionData?{
        val firesRecord = managed.exceptionData.firstOrNull()
       return if(firesRecord != null &&  managed.exceptionData.size == 1){
            firesRecord
        }else{
            null
        }
    }
    if (exception is ManagedException) {
        exception.setPropertySnapshot(snapshot)
        return  when (exception.handler) {
            HandlerType.SkipSelf -> {

                val exData  = firstOccurred(exception)
                if(exData != null){
                    container.source.changeStatus(ExecutionStatus.Failing)

                    container.source.warning(exception.throwableToText())
                    container.notifier.addErrorRecord(container.effectiveTask.createErrorSnapshot(), exception)
                }

                if (container.isRoot) {
                    val message = "Exception reached top. escalating"
                    container.source.error(message)
                    val exceptionData = ExceptionData(ManagedException.ExceptionEvent.Executed, message, container.source)
                    throw exception.addExceptionData(exceptionData, container.source)
                } else {
                    val message = "Rethrowing"
                    container.source.warning(message)
                    val exceptionData =
                        ExceptionData(ManagedException.ExceptionEvent.Rethrown, message, container.source)
                    return exception.addExceptionData(exceptionData, container.source)
                }
            }
            HandlerType.CancelAll ->{
                val exData  = firstOccurred(exception)
                if(exData != null){
                    container.source.changeStatus(ExecutionStatus.Failing)
                    container.source.error(exception.throwableToText())
                    val errorSnapshot = container.effectiveTask.createErrorSnapshot()
                    container.notifier.addErrorRecord(errorSnapshot, exception)
                }
                val message = "Reached RootTask<${container.effectiveTask}>"
                val data =  ExceptionData(ManagedException.ExceptionEvent.Executed,message,   container.source)
                exception.addExceptionData(data,  container.source)
            }
        }
    } else {
        val managed = exception.toManaged()
        container.source.changeStatus(ExecutionStatus.Failing)
        managed.setHandler(container.effectiveTask.config.exceptionHandler, container.source)
        container.source.error(exception.throwableToText())
        container.notifier.addErrorRecord(container.effectiveTask.createErrorSnapshot(), managed)
        return managed
    }
}

internal fun TaskBase<*, *>.createErrorSnapshot(): ErrorSnapshot{
    val compiledReport = createTaskData{
        val selectedSpans = actionSpans.selectUntil {
            (it.executionStatus == ExecutionStatus.Failing) || (it.executionStatus == ExecutionStatus.Faulty)
        }
        selectedSpans.map { it.createData() }
    }
    return compiledReport
}