package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.classes.taskresult.ManagedResult
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.models.TaskKey




suspend  fun <T, R: Any?> T.subTask(
    taskName: String,
    moduleName: String? = null,
    block: suspend  T.(TaskHandler<R>)-> R
):ManagedResult<R> {
    val task  = TasksManaged.attachToHierarchy<R>(taskName, moduleName).getOrThrowDefault("Child task creation failed")
    val runResult =  task.runTask(this ,block)
    val casted = runResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Cast to ManagedResult<R> failed")
    return  casted
}


suspend inline fun <R> TaskHandler<R>.withTask(
    key: TaskKey,
    block : suspend TaskSealedBase<R>.(TaskHandler<R>)-> R
): ManagedResult<R>{
    val task = TasksManaged.taskFromRegistry<R>(key).getOrThrowDefault("Task registry failure no key ${key.asString()} found")
    block.invoke(task, this)
    val casted =  task.taskResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Task registry failure no key ${key.asString()} found")
    return casted
}