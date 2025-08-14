package po.lognotify.dispatcher

import po.lognotify.TasksManaged
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.exceptions.getOrLoggerException
import po.lognotify.process.loggerProcess
import po.lognotify.process.processInContext
import po.lognotify.tasks.RootTask
import po.misc.context.CTX


/**
 * Creates a new root task in the task hierarchy.
 *
 * This is a shorthand for internal use that delegates to
 * [TasksManaged.LogNotify.taskDispatcher.createHierarchyRoot].
 *
 * @param name       The name of the root task.
 * @param receiver   The context object associated with the task.
 * @param taskConfig Configuration settings for the task.
 * @return The created root task.
 */
@PublishedApi
internal fun <T: CTX, R> createHierarchyRoot(
    name: String,receiver: T,
    taskConfig: TaskConfig
):RootTask<T, R>{
    val rootTask = TasksManaged.LogNotify.taskDispatcher.createHierarchyRoot<T, R>(name, receiver, taskConfig)
    with(rootTask){
        start()
        val emitter = dataProcessor.flowEmitter.getOrLoggerException(this)
        coroutineContext.loggerProcess()?.observeTask(emitter)
    }
    return rootTask
}

@PublishedApi
internal fun activeRootTask():RootTask<*, *>? = TasksManaged.LogNotify.taskDispatcher.activeRootTask()