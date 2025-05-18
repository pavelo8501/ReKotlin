package po.lognotify.extensions

import kotlinx.coroutines.runBlocking
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.models.TaskSettings
import po.lognotify.classes.task.result.toTaskResult
import po.lognotify.exceptions.handleException
import po.misc.coroutines.RunAsync


/**
 * Starts a root task using `runBlocking` and executes it synchronously.
 * Useful for top-level entry points, CLI tools, or unit tests where coroutine scope is not available.
 * Supports automatic retries if enabled via [settings].
 *
 * @param taskName Name of the task for logging and diagnostics.
 * @param settings Task behavior configuration including retry policy.
 * @param block The block to execute within the task context.
 * @return The result of the task, wrapped in [TaskResult].
 */
inline fun <reified T, R: Any?> T.runTaskBlocking(
    taskName: String,
    settings:TaskSettings = TaskSettings(),
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {

   val result = runBlocking {
       RunAsync(this){
           val moduleName: String = this::class.simpleName?:settings.moduleName
           val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
           try{
                block.invoke(this@runTaskBlocking, newTask.handler).toTaskResult(newTask)
            }catch (throwable: Throwable){
                throwable.handleException(this, newTask)
            }
        }
    }
    return result
}

/**
 * Starts a root task in the current coroutine context.
 * Use this variant when already inside a coroutine and need to execute a managed task asynchronously.
 * Supports automatic retries if enabled via [taskConfig].
 *
 * @param taskName Name of the task for logging and diagnostics.
 * @param taskConfig Task behavior configuration including retry policy.
 * @param block The block to execute within the task context.
 * @return The result of the task, wrapped in [TaskResult].
 */
suspend inline fun <reified T, R: Any?> T.runTaskAsync(
    taskName: String,
    taskConfig: TaskSettings = TaskSettings(),
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {

    val moduleName: String =  this::class.simpleName?:taskConfig.moduleName
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)

    var result = TaskResult<R>(newTask)
    RunAsync(this){
        result = try{
            block.invoke(this, newTask.handler).toTaskResult(newTask)
        }catch (throwable: Throwable){
            throwable.handleException(this, newTask)
        }
    }
    return result
}

/**
 * Starts a root task in a blocking (non-suspending) context with retry support.
 *
 * Intended for environments where suspending is not available.
 * Retries the block according to [settings.attempts] and [settings.delayMs].
 *
 * @param taskName Name of the task for logging and diagnostics.
 * @param settings Task behavior configuration including retry policy.
 * @param block The block to execute with contextual receivers [T] and [TaskHandler].
 * @return The result of the task, wrapped in [TaskResult].
 */
inline fun <reified T, R: Any?> T.runTask(
    taskName: String,
    settings: TaskSettings = TaskSettings(),
    block: context(T, TaskHandler<R>) ()-> R,
): TaskResult<R> {

    val moduleName: String = this::class.simpleName?:settings.moduleName
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, moduleName)
    var result : TaskResult<R>? = null

    repeat(settings.attempts) { attempt ->
        try {
            result = block.invoke(this, newTask.handler).toTaskResult(newTask)
            if(result.isSuccess){ return@repeat }
        }catch (throwable: Throwable){
            result = throwable.handleException(this, newTask)
            newTask.handler.warn("Task resulted in failure. Attempt $attempt of ${settings.attempts}")
        }
        if (attempt < settings.attempts - 1) {
            Thread.sleep(settings.delayMs)
        }
    }
  return result.getOrLoggerException("Maximum retries exceeded")
}







