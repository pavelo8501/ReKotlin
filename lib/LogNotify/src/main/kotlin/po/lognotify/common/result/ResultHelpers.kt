package po.lognotify.common.result

import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.Task
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.warn
import po.misc.data.helpers.wrapByDelimiter
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText
import po.misc.context.CTX
import po.misc.data.helpers.replaceIfNull
import kotlin.collections.joinToString

private fun <T: CTX, R> resultContainerCreation(task: TaskBase<T, R>, result: R): TaskResult<T, R>{
    task.registry.getFirstSubTask(task)?.let {subTask->
        if(subTask.executionStatus == ExecutionStatus.Faulty){
            val exception = subTask.taskResult?.throwable
            task.warn("Exception(${exception?.message}) swallowed by $subTask")
            val waypointInfo = exception?.exceptionData?.map { it  }?.joinToString(" -> ") { "${it}(${it.message.replaceIfNull()})" }
            waypointInfo?.wrapByDelimiter("->").replaceIfNull()
            task.warn(waypointInfo?.wrapByDelimiter("->").replaceIfNull())
        }
    }
    val result = TaskResult(task, result = result, throwable = null)
    task.taskResult = result
   return result
}

@PublishedApi
internal fun<T: CTX, R: Any?> createFaultyResult(managed: ManagedException, task: TaskBase<T, R>): TaskResult<T, R>{
    val faultyResult = TaskResult(task, throwable = managed)
    task.taskResult = faultyResult
    return faultyResult
}

@PublishedApi
internal fun <T: CTX, R> createTaskResult(result: R, task: TaskBase<T, R>): TaskResult<T, R>{
    val result = TaskResult(task, result = result, throwable = null)
   task.taskResult = result
   return result
}


fun <T: CTX, R> onTaskResult(task: TaskBase<T, R>, result: R): TaskResult<T, R>{
  return  when(result){
        is TaskResult<*, *> -> {
            when(result.task){
                is RootTask->{
                   val childException = task.checkChildResult(result)
                   if(childException != null){
                       val subTask = task.registry.getFirstSubTask(task)
                       task.warn("Exception ${childException.throwableToText()} swallowed by $subTask}")
                       createFaultyResult(childException, task)
                   }else{
                       task.dataProcessor.debug("SubTask has no faults. Why this branch called I don't know","ResultHelpers|onTaskResult")
                       resultContainerCreation(task, result)
                   }
                }
                is Task -> {
                    val childException = task.checkChildResult(result)
                    if(childException != null){
                        val subTask = task.registry.getFirstSubTask(task)
                        task.warn("Exception ${childException.throwableToText()} swallowed by $subTask}")
                        createFaultyResult(childException, task)
                    }else{
                        task.dataProcessor.debug("SubTask has no faults. Why this branch called I don't know","ResultHelpers|onTaskResult")
                        resultContainerCreation(task, result)
                    }
                }
            }
        }
        else -> {
            task.dataProcessor.debug("Result ok. ResultContainerCreation", "ResultHelpers|onTaskResult")
            resultContainerCreation(task, result)
        }
    }
}

inline fun <T: CTX,  R: Any?> TaskResult<T, R>.onFailureCause(block: T.(ManagedException) -> Unit): TaskResult<T, R> {
    throwable?.let {
        block.invoke(task.receiver, it)
    }
    return this
}

fun <T: CTX, R: Any?> TaskResult<T, R>.toKotlinResult(): Result<R?> =
    runCatching {  this.resultOrException() }


fun <T: CTX, R: Any?> TaskResult<T, R>.resultOrNull():R?{
   return this.result
}