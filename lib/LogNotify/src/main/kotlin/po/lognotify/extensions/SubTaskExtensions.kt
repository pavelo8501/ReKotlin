package po.lognotify.extensions

import kotlinx.coroutines.withContext
import po.lognotify.TasksManaged
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.createChild
import po.lognotify.classes.task.models.TaskSettings
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.result.toTaskResult
import po.lognotify.exceptions.handleException


suspend inline fun <reified T, R: Any?> T.subTaskAsync(
    taskName: String,
    taskConfig: TaskSettings = TaskSettings(),
    noinline block: suspend  T.(TaskHandler<R>)-> R
): TaskResult<R> {
    val rootTask = TasksManaged.taskDispatcher.activeRootTask()
    val moduleName: String =  this::class.simpleName?:taskConfig.moduleName
    val result = if(rootTask != null){
        val childTask = rootTask.createChild<R>(taskName, moduleName)
        withContext(childTask.coroutineContext){
            try {
                block.invoke(this@subTaskAsync, childTask.handler).toTaskResult(childTask)
            }catch (throwable: Throwable){
                throwable.handleException(this, childTask)
            }
        }
    }else{
        runTaskAsync(taskName, taskConfig, block)
    }
    return result
}


inline fun <reified T, R: Any?> T.subTask(
    taskName: String,
    taskConfig: TaskSettings = TaskSettings(),
    config: TaskSettings = TaskSettings(),
    block: context(T, TaskHandler<R>) () -> R
): TaskResult<R>{

    val rootTask = TasksManaged.taskDispatcher.activeRootTask()
    val moduleName = this::class.simpleName?:config.moduleName
    return if(rootTask != null){
        val childTask  =   rootTask.createChild<R>(taskName, moduleName)
        with(childTask) {
            try {
                block.invoke(this@subTask, childTask.handler).toTaskResult(childTask)
            }catch (throwable: Throwable){
                throwable.handleException(this, childTask)
            }
        }
    }else{
        this.runTask(taskName, taskConfig, block)
    }
}


