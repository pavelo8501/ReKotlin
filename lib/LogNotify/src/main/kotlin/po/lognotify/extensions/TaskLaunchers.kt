package po.lognotify.extensions

import kotlinx.coroutines.runBlocking
import po.lognotify.TaskProcessor
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.result.createFaultyResult
import po.lognotify.classes.task.result.onTaskResult
import po.lognotify.debug.DebugProxy
import po.lognotify.exceptions.handleException
import po.misc.coroutines.LauncherType
import po.misc.functions.repeatIfFaulty
import po.misc.interfaces.IdentifiableContext
import po.misc.reflection.properties.takePropertySnapshot


@PublishedApi
internal suspend fun <T: Any, R: Any?> taskRunner(receiver :T, newTask: RootTask<T, R>, block: suspend T.(TaskHandler<R>)-> R): TaskResult<R>{
  return  try{
      newTask.start()
      val value =  block.invoke(receiver, newTask.handler)
      val result = onTaskResult<T,R>(newTask, value)
      result
    }catch (throwable: Throwable){
      val snapshot = takePropertySnapshot<T, LogOnFault>(receiver)
      val managed = handleException(throwable, newTask, snapshot)
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
inline fun <reified T: Any, R: Any?> T.runTaskBlocking(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true),
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {

    var effectiveConfig = config
    val rootTask = TasksManaged.LogNotify.taskDispatcher.activeRootTask()
    if(rootTask != null && config.isDefault){
        effectiveConfig = rootTask.config
    }

   val receiver = this
   val result = runBlocking {
       val moduleName: String = this::class.simpleName?:config.moduleName
       val dispatcher = TasksManaged.LogNotify.taskDispatcher
       val newTask = dispatcher.createHierarchyRoot<T, R>(taskName, moduleName, effectiveConfig, this@runTaskBlocking)
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
suspend inline fun <reified T: Any, R: Any?> T.runTaskAsync(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true),
    noinline block: suspend T.(TaskHandler<R>)-> R,
): TaskResult<R> {


    var effectiveConfig = config
    val rootTask = TasksManaged.LogNotify.taskDispatcher.activeRootTask()
    if(rootTask != null && config.isDefault){
        effectiveConfig = rootTask.config
    }

    val receiver = this
    val moduleName: String = this::class.simpleName ?: config.moduleName
    val dispatcher = TasksManaged.LogNotify.taskDispatcher
    val newTask = dispatcher.createHierarchyRoot<T, R>(taskName, moduleName, effectiveConfig, this)
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
inline fun <reified T: Any, R: Any?> T.runTask(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true),
    debugProxy: DebugProxy<*,*>? = null,
    block: T.(TaskHandler<R>)-> R,
): TaskResult<R> {

    if(debugProxy != null){
        debugProxy.methodName = taskName
    }

    var effectiveConfig = config
    val dispatcher = TasksManaged.LogNotify.taskDispatcher
    val rootTask =  dispatcher.activeRootTask()
    if(rootTask != null && config.isDefault){
        effectiveConfig = rootTask.config
    }
    val moduleName: String = this::class.simpleName?:config.moduleName
    val task = dispatcher.createHierarchyRoot<T, R>(taskName, moduleName, effectiveConfig, this)
    task.start()
    val result = repeatIfFaulty(times =  config.attempts, actionOnFault = {Thread.sleep(config.delayMs)}){attempt->
        try {
            val lambdaResult = block.invoke(this, task.handler)
            onTaskResult(task, lambdaResult)
        }catch (throwable: Throwable){
            val snapshot = takePropertySnapshot<T, LogOnFault>(this)
            val managed = handleException(throwable, task, snapshot)
            if (config.attempts > 1) {
                task.handler.warn("Task resulted in failure. Attempt $attempt of ${config.attempts}")
            }
            task.dataProcessor.debug("Throwable in catch block", "TaskLauncher|runTask")
            task.taskResult?:run{
                throw managed
            }
        }finally {
            task.complete()
        }
    }
    return result

//
//    repeat(config.attempts) { attempt ->
//        try {
//            val lambdaResult = block.invoke(this, task.handler)
//            val result = onTaskResult(task, lambdaResult)
//            task.dataProcessor.debug("Created result  by onTaskResult", "TaskLauncher|runTask", task)
//            return result
//        }catch (throwable: Throwable){
//            val snapshot = takePropertySnapshot<T, LogOnFault>(this)
//            val managed = handleException(throwable, task, snapshot)
//            task.dataProcessor.debug("Throwable in catch block", "TaskLauncher|runTask", task)
//
//            result = createFaultyResult(managed, task)
//            if(config.attempts > 0){
//                val attemptCount = attempt + 1
//                task.handler.warn("Task resulted in failure. Attempt $attemptCount of ${config.attempts}")
//                if (attempt < config.attempts - 1) {
//                    Thread.sleep(config.delayMs)
//                }
//            }
//        }finally {
//            task.complete()
//        }
//    }

//  val testResult = result?.resultOrException()
//  return result.getOrLoggerException("Maximum retries exceeded")
}







