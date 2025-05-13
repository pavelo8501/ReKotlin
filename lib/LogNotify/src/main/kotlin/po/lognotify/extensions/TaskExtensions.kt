package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import po.lognotify.TasksManaged
import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskResult
import po.lognotify.classes.task.TaskSealedBase
import kotlin.coroutines.CoroutineContext


//suspend  fun <T, R: Any?> T.startTask(
//    taskName: String,
//    coroutine: CoroutineContext,
//    moduleName: String? = null,
//    block: suspend T.(TaskHandler<R>)-> R,
//): TaskResult<R> {
//    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, coroutine, moduleName)
//    val runResult = newTask.runTask(this, block)
//    val casted = runResult.castOrLoggerException<TaskResult<R>>()
//    return casted
//}
//
//fun <T, R> T.startTaskAsync(
//    taskName: String,
//    moduleName: String? = null,
//    block: suspend T.(TaskHandler<R>)-> R,
//): TaskResult<R> {
//    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
//    val runResult =  newTask.runTaskAsync(this@startTaskAsync, block)
//    return runResult
//}


fun <T, R> T.newTaskAsync(
    taskName: String,
    moduleName: String,
    block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
    val runResult =  newTask.runTaskAsync(this@newTaskAsync, block)
    return runResult
}

suspend inline fun <reified T, R> T.newTask(
    taskName: String,
    coroutine: CoroutineContext,
    moduleName: String? = null,
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {
    val moduleName: String  =  moduleName?:this::class.simpleName.toString()
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, coroutine, moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    return runResult
}


suspend inline fun <T: CoroutineContext, R> T.newTask(
    taskName: String,
    moduleName: String,
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, this, moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    return runResult
}

@JvmName("newTaskOnCoroutineScope")
suspend inline fun <reified T: CoroutineScope, R>  T.newTask(
    taskName: String,
    moduleName: String? = null,
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {
    val moduleName: String = moduleName?: this::class.simpleName.toString()
    val exist =  coroutineContext[LoggProcess]
    println("Process")
    println(exist)

    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, this.coroutineContext, moduleName)
    val runResult = newTask.runTaskInlined(this, block)
    return runResult
}








