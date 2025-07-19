package po.lognotify.exceptions

import po.lognotify.TasksManaged
import po.lognotify.action.ActionSpan
import po.lognotify.common.containers.ActionContainer
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.common.containers.TaskContainer
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.models.TaskData
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.models.ExceptionData
import po.misc.exceptions.throwableToText
import po.misc.types.selectToInstance


@PublishedApi
internal fun <T: TasksManaged, R: Any?> handleException(
    exception: Throwable,
    container: RunnableContainer<T, R>,
    snapshot: List<PropertyData>?
): ManagedException {

    if (exception is ManagedException) {
        exception.setPropertySnapshot(snapshot)
        return   when (exception.handler) {
            HandlerType.Undefined ->{
                val exceptionHandler = container.effectiveTask.config.exceptionHandler
                when(container){
                    is ActionContainer->{
                        exception.setHandler(exceptionHandler, container.actionSpan.receiver, container.actionSpan)
                    }
                    is TaskContainer<*, *> ->{
                        exception.setHandler(exceptionHandler, container.effectiveTask.ctx, container.effectiveTask)
                    }
                }
                container.effectiveTask.dataProcessor.error(exception)
                exception
            }
            HandlerType.SkipSelf ->{

                val exceptionExecutedMsg = "Exception reached top. escalating"
                when(container){
                    is ActionContainer->{
                        if(container.isRoot){
                            val spanMsg = "${exceptionExecutedMsg}. Run by ${container.actionSpan.toString()}. Could have tried fallback"
                            val data =  ExceptionData.createExecutedEvent(container.actionSpan.receiver, container.actionSpan, spanMsg)
                            exception.addHandlingData(data)
                            container.effectiveTask.dataProcessor.warn(spanMsg)
                            throw exception
                        }else{
                            val message = "Rethrowing"
                            container.effectiveTask.dataProcessor.warn(exception, message)
                            val data =  ExceptionData.createRethrownEvent(container.actionSpan.receiver, container.actionSpan, message)
                            exception.addHandlingData(data)
                            return exception
                        }
                    }
                    is TaskContainer<*, *> ->{
                       if(container.isRoot){
                           val data =  ExceptionData.createExecutedEvent(container.sourceTask.ctx, container.sourceTask, exceptionExecutedMsg)
                           exception.addHandlingData(data)
                           container.effectiveTask.dataProcessor.warn(exceptionExecutedMsg)
                           throw exception
                       }else{
                           val message = "Rethrowing"
                           container.effectiveTask.dataProcessor.warn(exception, message)
                           val data =  ExceptionData.createRethrownEvent(container.sourceTask.ctx, container.sourceTask, message)
                           exception.addHandlingData(data)
                           return exception
                       }
                    }
                }
            }
            HandlerType.CancelAll ->{
                if(container.effectiveTask is RootTask){
                    val message = "Reached RootTask<${container.effectiveTask.ctx.contextName}>"
                    val data =  ExceptionData.createExecutedEvent(container.effectiveTask.ctx, container.effectiveTask, message)
                    exception.addHandlingData(data)
                }else{
                    val message = "Rethrowing"
                    container.effectiveTask.dataProcessor.warn(exception, message)
                    val data =  ExceptionData.createRethrownEvent(container.effectiveTask.ctx, container.effectiveTask, message)
                    exception.addHandlingData(data)
                }
            }
        }
    } else {
        val exceptionHandler = container.effectiveTask.config.exceptionHandler
        val managed = ManagedException(exception.throwableToText(), null, exception)
        when(container){
            is ActionContainer->{
                managed.setHandler(exceptionHandler, container.actionSpan.receiver, container.actionSpan)
                managed.setPropertySnapshot(snapshot)

                val backtraceData = container.provideBackTrace(managed)
                managed.addBackTraceRecord(backtraceData, container.actionSpan)
            }
            is TaskContainer<*, *> -> {
                managed.setHandler(exceptionHandler, container.effectiveTask.ctx, container.effectiveTask)
                managed.setPropertySnapshot(snapshot)
            }
        }
        container.effectiveTask.dataProcessor.error(managed)
        return managed
    }
}

fun <T: TasksManaged, R: Any?> RunnableContainer<T, R>.provideBackTrace(managed: ManagedException): TaskData{
    val activeSpans: MutableList<ActionSpan<*,*>> = mutableListOf()
    effectiveActionSpan?.let {
        effectiveTask.actionSpans.selectToInstance(activeSpans, it)
    }
    val spanDataRecords =  activeSpans.map { it.createData() }
    val taskData = effectiveTask.createTaskData(spanDataRecords)
    managed.addBackTraceRecord(taskData, effectiveTask)
    return taskData
}

