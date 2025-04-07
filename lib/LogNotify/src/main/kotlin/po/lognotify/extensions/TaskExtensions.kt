package po.lognotify.extensions

import kotlinx.coroutines.runBlocking
import po.lognotify.TasksManaged
import po.lognotify.classes.ManagedResult
import po.lognotify.classes.task.TaskHandler
import kotlin.coroutines.CoroutineContext


suspend  fun <T, R: Any?> T.startTask(
    taskName: String,
    coroutine: CoroutineContext,
    block: suspend   T.(TaskHandler)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, coroutine)
    newTask.initializeComponents()
    val runResult = newTask.runTask(this, block)
    val casted = runResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Cast to ManagedResult<R> failed")
    return casted
}

fun <T, R> T.startTaskAsync(
    taskName: String,
    block: suspend T.(TaskHandler)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName)
    val runResult =  runBlocking {
        newTask.initializeComponents()
        newTask.runTaskInDefaultContext(this@startTaskAsync, block)
    }
    val casted = runResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Cast to ManagedResult<R> failed")
    return casted
}

inline fun <T, R> TaskHandler.withTxScope(scope: T, block: T.() -> R): R = block(scope)



