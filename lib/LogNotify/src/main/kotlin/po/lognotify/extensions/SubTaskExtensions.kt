package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.classes.ManagedResult
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.models.TaskKey


suspend  fun <R: Any?>  TasksManaged.subTask(
    taskName: String,
    block: suspend  (TaskHandler)-> R
): ManagedResult<R> {
    val task  = TasksManaged.attachToHierarchy<R>(taskName).getOrThrowDefault("Child task creation failed")
    val runResult =  task.runTask(block)
    val casted = runResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Cast to ManagedResult<R> failed")
    return casted
}

suspend  fun <T, R: Any?>  T.subTaskWithReceiver(
    taskName: String,
    block: suspend  T.(TaskHandler)-> R
):ManagedResult<R> {
    val task  = TasksManaged.attachToHierarchy<R>(taskName).getOrThrowDefault("Child task creation failed")
    val runResult =  task.runTask(this ,block)
    val casted = runResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Cast to ManagedResult<R> failed")
    return  casted
}


suspend inline fun <R> TaskHandler.withTask(
    key: TaskKey,
    block : suspend TaskSealedBase<R>.(TaskHandler)-> R
): ManagedResult<R>{
    val task = TasksManaged.taskFromRegistry<R>(key).getOrThrowDefault("Task registry failure no key ${key.asString()} found")
    block.invoke(task, task.helper)
    val casted =  task.taskResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Task registry failure no key ${key.asString()} found")
    return casted
}