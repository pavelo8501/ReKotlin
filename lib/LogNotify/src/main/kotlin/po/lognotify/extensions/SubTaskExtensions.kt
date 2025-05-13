package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskResult

suspend  fun <T, R> T.subTask(
    taskName: String,
    moduleName: String? = null,
    block: suspend  T.(TaskHandler<R>)-> R
): TaskResult<R> {

    return TasksManaged.attachToHierarchy<R>(taskName, moduleName)?.let {
        val taskResult = it.runTask(this ,block)
        taskResult
    }?:run {
       val rootTaskResult  = this.newTaskAsync(taskName, moduleName?:"N/A", block)
       rootTaskResult.task.notifier.warn("Task created as substitution for SubTask. Consider restructure")
       return rootTaskResult
    }
}

//suspend  fun <T, R> T.withLastTask(
//    block: suspend  T.(TaskHandler<R>)-> R
//):R? {
//    val lastTask  = TasksManaged.continueWithLastTask<R>()
//    block.invoke(this, lastTask.taskHandler)
//    return lastTask.taskResult.resultOrNull()
//}

fun lastTaskHandler():TaskHandler<*> {
    val lastTaskHandler  = TasksManaged.getLastTaskHandler()
    return lastTaskHandler
}