package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import po.lognotify.TasksManaged
import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.task.RootSyncTaskHandler
import po.lognotify.classes.task.RootTaskSync
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskHandlerBase
import po.lognotify.classes.task.TaskResult
import po.lognotify.classes.task.TaskResultSync
import po.lognotify.classes.task.createTaskKey
import kotlin.coroutines.CoroutineContext


data class TaskSettings(
    val attempts: Int = 1,
    val delayMs: Long = 2000
)


fun <T, R> T.newTaskSync(
    taskName: String,
    moduleName: String,
    settings:TaskSettings = TaskSettings(),
    block: context(T, TaskHandlerBase<R>) ()-> R,
): TaskResultSync<R> {

    val newTask = TasksManaged.createHierarchyRootSynced<R>(RootTaskSync(createTaskKey(taskName, moduleName)))
    var result : TaskResultSync<R>? = null

    repeat(settings.attempts) { attempt ->
        try {
            result = block.invoke(this, newTask.taskHandler).toResult(newTask)
            if(result.isSuccess){ return@repeat }
        }catch (th: Throwable){
            result = th.handleException(newTask).toResult()
            newTask.taskHandler.warn("Task resulted in failure. Attempt $attempt of ${settings.attempts}")
        }
        if (attempt < settings.attempts - 1) {
            Thread.sleep(settings.delayMs)
        }
    }
  return result.getOrLoggerException("Maximum retries exceeded")
}

fun <T, R> T.newTaskAsync(
    taskName: String,
    moduleName: String,
    settings:TaskSettings = TaskSettings(),
    block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
    val runResult = newTask.runTaskAsync(this@newTaskAsync, block)
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








