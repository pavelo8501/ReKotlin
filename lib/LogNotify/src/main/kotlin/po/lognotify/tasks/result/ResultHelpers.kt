package po.lognotify.tasks.result

import po.lognotify.tasks.RootTask
import po.lognotify.tasks.Task
import po.lognotify.tasks.TaskBase
import po.misc.data.helpers.emptyOnNull
import po.misc.data.helpers.wrapByDelimiter
import po.misc.exceptions.ManagedException
import po.misc.exceptions.name
import kotlin.collections.joinToString

private fun <T, R> resultContainerCreation(task: TaskBase<T, R>, result: R): TaskResult<R>{
    task.registry.getFirstSubTask(task)?.let {subTask->
        if(subTask.taskStatus == TaskBase.TaskStatus.Faulty){
            val exception = subTask.taskResult?.throwable
            task.dataProcessor.warn("Exception(${exception?.message}) swallowed by $subTask")
            val waypointInfo = exception?.handlingData?.joinToString(" -> ") { "${it.wayPoint.contextName}(${it.message.emptyOnNull()})" }
            waypointInfo?.wrapByDelimiter("->").emptyOnNull()
            task.dataProcessor.warn(waypointInfo?.wrapByDelimiter("->").emptyOnNull())
        }
    }
    val result = TaskResult(task, result = result, throwable = null)
    task.taskResult = result
   return result
}


fun<R: Any?> createFaultyResult(managed: ManagedException, task: TaskBase<*, R>): TaskResult<R>{
    val faultyResult = TaskResult(task, throwable = managed)
    task.taskResult = faultyResult
    return faultyResult
}

fun <T, R> onTaskResult(task: TaskBase<T, R>, result: R): TaskResult<R>{
  return  when(result){
        is TaskResult<*> -> {
            when(result.task){
                is RootTask->{
                   val childException = task.checkChildResult(result)
                   if(childException != null){
                       val subTask = task.registry.getFirstSubTask(task)
                       task.dataProcessor.warn("Exception ${childException.name()} swallowed by $subTask}")
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
                        task.dataProcessor.warn("Exception ${childException.name()} swallowed by $subTask}")
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

inline fun <R: Any?> TaskResult<R>.onFailureCause(block: (ManagedException) -> Unit): TaskResult<R> {
    throwable?.let {
        task.dataProcessor.errorHandled("onFailureCause", it)
        block.invoke(it)
    }
    return this
}

fun <R: Any?> TaskResult<R>.toKotlinResult(): Result<R?> =
    runCatching {  this.resultOrException() }


fun <R: Any?> TaskResult<R>.resultOrNull():R?{
   return this.result
}