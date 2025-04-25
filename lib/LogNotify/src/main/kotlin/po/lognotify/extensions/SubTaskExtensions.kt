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
        val TaskResult = it.runTask(this ,block)
        TaskResult
    }?:run {
       return this.startTaskAsync(taskName, moduleName, block)
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