package po.lognotify.exceptions

import po.lognotify.TasksManaged
import po.lognotify.notification.models.ErrorSnapshot
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.notification.error
import po.lognotify.notification.warning
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.Task
import po.lognotify.tasks.TaskBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.collections.selectUntil
import po.misc.exceptions.throwableToText
import po.misc.exceptions.toManaged


@PublishedApi
internal fun <T: TasksManaged, R: Any?> handleException(
    exception: Throwable,
    container: RunnableContainer<T, R>,
    snapshot: List<PropertyData>?
): ManagedException {

    if (exception is ManagedException) {
        exception.setPropertySnapshot(snapshot)

        if(isManagedFirstOccurred(container.effectiveTask, exception)){
            container.notifier.addErrorRecord(container.effectiveTask.createErrorSnapshot(), exception)
        }

        return  when (exception.handler) {
            HandlerType.SkipSelf -> {
                if (container.isRoot) {
                    val message = "Exception reached top. escalating"
                    container.source.error(message)
                    return exception
                } else {
                    val message = "Rethrowing"
                    container.source.warning(message)
                    return exception
                }
            }
            HandlerType.CancelAll ->{
                exception
            }
        }
    } else {
        val managed = exception.toManaged(container.source.receiver)
        container.source.changeStatus(ExecutionStatus.Failing)
        managed.setHandler(container.effectiveTask.config.exceptionHandler, container.source)
        container.source.error(exception.throwableToText())
        container.notifier.addErrorRecord(container.effectiveTask.createErrorSnapshot(), managed)
        return managed
    }
}

internal fun isManagedFirstOccurred(task: TaskBase<*, *>, managed: ManagedException): Boolean{
   return if(task.executionStatus  == ExecutionStatus.Active || task.executionStatus  == ExecutionStatus.Complete){
      val faultyTask = task.registry.tasks.values.firstOrNull {
           it.executionStatus ==  ExecutionStatus.Failing || it.executionStatus ==  ExecutionStatus.Faulty
       }
       faultyTask == null
    }else{
        false
    }
}

@PublishedApi
internal fun failRationale(task: TaskBase<*, *>, managed: ManagedException): FailHandlingRationale{
    return  when(task){
        is RootTask->{
            FailHandlingRationale(isRootTask =  true, coroutineOwner=  false, 0, managed)
        }
        is Task<*, *> -> {
            FailHandlingRationale(isRootTask = false, coroutineOwner = false, task.key.nestingLevel, managed)
        }
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