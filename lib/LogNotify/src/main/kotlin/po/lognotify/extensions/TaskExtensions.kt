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
    val casted = runResult.castOrThrow<ManagedResult<R>>("Cast to ManagedResult<R> failed")
    return casted
}

fun <T, R> T.startTaskAsync(
    taskName: String,
    moduleName: String? = null,
    block: suspend T.(TaskHandler<R>)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
    val runResult =  newTask.runTaskAsync(this@startTaskAsync, block)
    val casted = runResult.castOrThrow<ManagedResult<R>>("Cast to ManagedResult<R> failed")
    return casted
}


inline suspend fun <reified T, R: Any?> T.newTask(
    taskName: String,
    noinline block: suspend T.()-> R,
): ManagedResult<R> {
    val moduleName: String = this::class.simpleName.toString()
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName,  moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    val casted = runResult.castOrThrow<ManagedResult<R>>("Cast to ManagedResult<R> failed")
    return casted
}



inline suspend fun <reified T: CoroutineContext, R: Any?> T.newTask(
    taskName: String,
    noinline block: suspend T.()-> R,
): ManagedResult<R> {
    val moduleName: String = this::class.simpleName.toString()
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, this, moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    val casted = runResult.castOrThrow<ManagedResult<R>>("Cast to ManagedResult<R> failed")
    return casted
}

@JvmName("newTaskOnCoroutineScope")
inline suspend fun <reified T: CoroutineScope, R: Any?>  T.newTask(
    taskName: String,
    noinline block: suspend CoroutineScope.()-> R,
): ManagedResult<R> {
    val moduleName: String = this::class.simpleName.toString()
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, this.coroutineContext, moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    val casted = runResult.castOrThrow<ManagedResult<R>>("Cast to ManagedResult<R> failed")
    return casted
}








