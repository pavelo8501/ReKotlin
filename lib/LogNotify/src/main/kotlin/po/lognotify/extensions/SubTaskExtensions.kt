package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.classes.task.ManagedTask
import po.lognotify.classes.taskresult.ManagedResult
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.models.TaskKey

suspend  fun <T, R: Any?> T.subTask(
    taskName: String,
    moduleName: String? = null,
    block: suspend  T.(TaskHandler<R>)-> R
):ManagedResult<R> {
    val task  = TasksManaged.attachToHierarchy<R>(taskName, moduleName).getOrThrow("Child task creation failed")
    val runResult =  task.runTask(this ,block)
    val casted = runResult.castOrThrow<ManagedResult<R>>("Cast to ManagedResult<R> failed")
    return  casted
}

suspend  fun <T, R> T.withLastTask(
    block: suspend  T.(TaskHandler<R>)-> R
):R? {
    val lastTask  = TasksManaged.continueWithLastTask<R>()
    block.invoke(this, lastTask.taskHandler)
    return lastTask.taskResult.resultOrNull()
}

fun lastTaskHandler():TaskHandler<*> {
    val lastTaskHandler  = TasksManaged.getLastTaskHandler()
    return lastTaskHandler
}