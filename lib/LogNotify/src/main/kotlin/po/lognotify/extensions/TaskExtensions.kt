package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.classes.taskresult.ManagedResult
import po.lognotify.classes.task.TaskHandler
import kotlin.coroutines.CoroutineContext


suspend  fun <T, R: Any?> T.startTask(
    taskName: String,
    coroutine: CoroutineContext,
    moduleName: String?,
    block: suspend   T.(TaskHandler<R>)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, coroutine, moduleName)
    val runResult = newTask.runTask(this, block)
    val casted = runResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Cast to ManagedResult<R> failed")
    return casted
}

fun <T, R> T.startTaskAsync(
    taskName: String,
    moduleName: String? = null,
    block: suspend T.(TaskHandler<R>)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
    val runResult =  newTask.runTaskAsync(this@startTaskAsync, block)
    val casted = runResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Cast to ManagedResult<R> failed")
    return casted
}








