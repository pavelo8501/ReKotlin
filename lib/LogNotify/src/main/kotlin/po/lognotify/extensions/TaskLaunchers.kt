package po.lognotify.extensions

import kotlinx.coroutines.runBlocking
import po.lognotify.TasksManaged
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.result.createFaultyResult
import po.lognotify.classes.task.result.onTaskResult
import po.lognotify.exceptions.handleException
import po.misc.coroutines.LauncherType


@PublishedApi
internal suspend fun <T, R: Any?> taskRunner(receiver :T, newTask: RootTask<T, R>, block: suspend T.(TaskHandler<R>)-> R): TaskResult<R>{
  return  try{
      newTask.start()
      val value =  block.invoke(receiver, newTask.handler)
      val result = onTaskResult<T,R>(newTask, value)
      result
    }catch (throwable: Throwable){
       val managed = handleException(throwable, newTask)
       createFaultyResult(managed, newTask)

    }finally {
      newTask.complete()
    }
}

/**
 * Runs a managed root task in a blocking manner using [runBlocking].
 *
 * This is intended for **entry points**, **synchronous flows**, or **tests**
 * where a coroutine context is not available or required.
 *
 * The task is launched using the launcher strategy specified in [config.launcherType]
 * and is executed with the [config.dispatcher] applied.
 *
 * ### Configuration:
 * - `attempts`: Number of retry attempts if the task fails (default: `1`)
 * - `delayMs`: Delay between retries in milliseconds (default: `2000`)
 * - `moduleName`: Optional override for module identification
 * - `dispatcher`: Coroutine dispatcher used for task execution
 * - `launcherType`: Controls coroutine behavior — Async or Concurrent
 *
 * @param taskName Name of the task, used for diagnostics and logging.
 * @param config Optional [TaskConfig] specifying retry and coroutine behavior.
 * @param block The task logic executed with a [TaskHandler] injected.
 * @return [TaskResult] wrapping the output or captured exception.
 *
 * @see TaskConfig
 * @see LauncherType
 */
inline fun <reified T, R: Any?> T.runTaskBlocking(
    taskName: String,
    config: TaskConfig = TaskConfig(),
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {

   val receiver = this
   val result = runBlocking {
       val moduleName: String = this::class.simpleName?:config.moduleName
       val newTask = TasksManaged.createHierarchyRoot<T, R>(taskName, moduleName, config, this@runTaskBlocking)
       when(config.launcherType){
           is LauncherType.AsyncLauncher -> {
               (config.launcherType as LauncherType.AsyncLauncher).RunCoroutineHolder(newTask, config.dispatcher){
                  taskRunner(receiver, newTask, block)
               }
           }
           is LauncherType.ConcurrentLauncher -> {
               (config.launcherType as LauncherType.ConcurrentLauncher).RunCoroutineHolder(newTask, config.dispatcher){
                   taskRunner(receiver, newTask, block)
               }
           }
       }
    }
    return result
}

/**
 * Launches a managed root task within the **current coroutine context**.
 *
 * This is suitable for calling from **suspend functions** and coroutine-aware flows.
 * Uses the [config.launcherType] to determine coroutine launching strategy (default: Async).
 *
 * ### Configuration:
 * - `attempts`: Retry attempts in case of failure
 * - `delayMs`: Optional delay between retries
 * - `dispatcher`: Dispatcher applied to the coroutine scope
 * - `launcherType`: Defines if coroutine runs in current (`Async`) or new (`Concurrent`) context
 *
 * ### Usage:
 * ```kotlin
 * runTaskAsync("my-task", TaskConfig(attempts = 3)) {
 *     // your logic here
 * }
 * ```
 *
 * @param taskName Logical name of the task, useful for tracing.
 * @param config Optional [TaskConfig] to customize execution.
 * @param block The block to execute with a [TaskHandler] injected.
 * @return [TaskResult] containing success value or captured error.
 *
 * @see TaskConfig
 * @see LauncherType
 */
suspend inline fun <reified T, R: Any?> T.runTaskAsync(
    taskName: String,
    config: TaskConfig = TaskConfig(),
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {

    val receiver = this
    val moduleName: String = this::class.simpleName ?: config.moduleName
    val newTask = TasksManaged.createHierarchyRoot<T, R>(taskName, moduleName, config, this)
    return when (config.launcherType) {
        is LauncherType.AsyncLauncher -> {
            (config.launcherType as LauncherType.AsyncLauncher).RunCoroutineHolder(newTask, config.dispatcher) {
                taskRunner(receiver, newTask, block)
            }
        }
        is LauncherType.ConcurrentLauncher -> {
            (config.launcherType as LauncherType.ConcurrentLauncher).RunCoroutineHolder(newTask, config.dispatcher) {
                taskRunner(receiver, newTask, block)
            }
        }
    }
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
    config: TaskConfig = TaskConfig(),
   crossinline block: T.(TaskHandler<R>)-> R,
): TaskResult<R> {

    val moduleName: String = this::class.simpleName?:config.moduleName
    val task = TasksManaged.createHierarchyRoot<T, R>(taskName, moduleName, config, this)

    var result : TaskResult<R>? = null
    task.start()
    repeat(config.attempts) { attempt ->
        try {
            val lambdaResult = block.invoke(this, task.handler)
            val result = onTaskResult(task, lambdaResult)
            task.dataProcessor.info("Created result  by onTaskResult", task)
            return result
        }catch (throwable: Throwable){
            val managed = handleException(throwable, task)
            task.dataProcessor.info("Throwable in catch block", task)

            result = createFaultyResult(managed, task)
            val attemptCount = attempt + 1
            task.handler.warn("Task resulted in failure. Attempt $attemptCount of ${config.attempts}")
            if (attempt < config.attempts - 1) {
                Thread.sleep(config.delayMs)
            }
        }finally {
            task.complete()
        }
    }
  return result.getOrLoggerException("Maximum retries exceeded")
}







