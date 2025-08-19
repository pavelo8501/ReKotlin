package po.lognotify.launchers

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.common.configuration.TaskType
import po.lognotify.common.containers.TaskContainer
import po.lognotify.common.result.TaskResult
import po.lognotify.common.result.createFaultyResult
import po.lognotify.common.result.createTaskResult
import po.lognotify.common.result.onTaskResult
import po.lognotify.debug.DebugProxy
import po.lognotify.dispatcher.activeRootTask
import po.lognotify.dispatcher.activeTask
import po.lognotify.dispatcher.createHierarchyRoot
import po.lognotify.exceptions.handleException
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.Task
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.TaskHandler
import po.lognotify.tasks.createTask
import po.misc.coroutines.LauncherType
import po.misc.functions.repeater.repeatOnFault
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.properties.takePropertySnapshot

@PublishedApi
internal suspend fun <T : TasksManaged, R : Any?> taskRunner(
    newTask: TaskBase<T, R>,
    block: suspend () -> R,
): TaskResult<T, R> =
     try {
         when(newTask){
            is RootTask -> {
                newTask.scope.async {
                    val lambdaResult = block.invoke()
                    val result = onTaskResult(newTask, lambdaResult)
                    newTask.complete(result)
                }.await()
            }
            is Task -> {
                val lambdaResult = block.invoke()
                val result = onTaskResult(newTask, lambdaResult)
                newTask.complete(result)
            }
        }
    } catch (throwable: Throwable) {
        val snapshot = takePropertySnapshot<T, LogOnFault>(newTask.receiver)
        val container = TaskContainer.create<T, R>(newTask)
        val managed = handleException(throwable, container, snapshot)
        createFaultyResult(managed, newTask)
        newTask.complete(managed)
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
inline fun <reified T : TasksManaged, R : Any?> T.runTaskBlocking(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true, taskType = TaskType.AsRootTask),
    noinline block: suspend T.() -> R,
): TaskResult<T, R> {

    return runBlocking {
        val newTask: RootTask<T, R> = createHierarchyRoot(taskName, this@runTaskBlocking, config)
        taskRunner(newTask){
            block.invoke(this@runTaskBlocking)
        }
    }
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
suspend inline fun <reified T : TasksManaged, R : Any?> T.runTaskAsync(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true),
    noinline block: suspend T.() -> R,
): TaskResult<T, R> {

    val activeRoot = activeRootTask()
   val newTask = activeRoot.createTask<T , R>(taskName, this, config)
   return taskRunner(newTask){
        block(newTask.receiver)
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
inline fun <T : TasksManaged, reified R : Any?> T.runTask(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true),
    debugProxy: DebugProxy<*, *>? = null,
    crossinline block: T.() -> R,
): TaskResult<T, R> {

    val newTask: TaskBase<T, R> = activeTask().createTask(taskName, this, config)
    newTask.start()
    if (debugProxy != null) {
        debugProxy.provideDataProcessor(newTask.dataProcessor)
        debugProxy.methodName = taskName
    }

    val taskContainer: TaskContainer<T, R> = TaskContainer.create<T, R>(newTask)
    taskContainer.classInfoProvider.registerProvider { overallInfo<R>(ClassRole.Result) }

    val result = repeatOnFault({
        setMaxAttempts(config.attempts).onException { stats ->
            taskContainer.sourceTask.collectException(stats.exception)
            if(stats.isLastAttempt){
                val managed = handleException(stats.exception, taskContainer, null)
                createFaultyResult(managed, taskContainer.sourceTask)
                taskContainer.sourceTask.complete(managed)
            }
        }
    }){
        val lambdaResult = this.block()
        createTaskResult(lambdaResult, newTask)
    }
    newTask.complete()
    return result
}
