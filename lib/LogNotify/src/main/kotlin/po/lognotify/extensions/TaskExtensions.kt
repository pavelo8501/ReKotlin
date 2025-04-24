package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import po.lognotify.TasksManaged
import po.lognotify.classes.taskresult.ManagedResult
import po.lognotify.classes.task.TaskHandler
import kotlin.coroutines.CoroutineContext


suspend  fun <T, R: Any?> T.startTask(
    taskName: String,
    coroutine: CoroutineContext,
    moduleName: String? = null,
    block: suspend T.(TaskHandler<R>)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, coroutine, moduleName)
    val runResult = newTask.runTask(this, block)
    val casted = runResult.castOrLoggerException<ManagedResult<R>>()
    return casted
}

fun <T, R> T.startTaskAsync(
    taskName: String,
    moduleName: String? = null,
    block: suspend T.(TaskHandler<R>)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
    val runResult =  newTask.runTaskAsync(this@startTaskAsync, block)
    val casted = runResult.castOrLoggerException<ManagedResult<R>>()
    return casted
}


suspend inline fun <reified T, R> T.newTask(
    taskName: String,
    noinline block: suspend T.()-> R,
): ManagedResult<R> {
    val moduleName: String = this::class.simpleName.toString()
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName,  moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    val casted = runResult.castOrLoggerException<ManagedResult<R>>()
    return casted
}


suspend inline fun <T: CoroutineContext, R> T.newTask(
    taskName: String,
    moduleName: String,
    noinline block: suspend T.()-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, this, moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    val casted = runResult.castOrLoggerException<ManagedResult<R>>()
    return casted
}

@JvmName("newTaskOnCoroutineScope")
suspend inline fun <reified T: CoroutineScope, R>  T.newTask(
    taskName: String,
    noinline block: suspend T.()-> R,
): ManagedResult<R> {
    val moduleName: String = this::class.simpleName.toString()
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, this.coroutineContext, moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    val casted = runResult.castOrLoggerException<ManagedResult<R>>()
    return casted
}








