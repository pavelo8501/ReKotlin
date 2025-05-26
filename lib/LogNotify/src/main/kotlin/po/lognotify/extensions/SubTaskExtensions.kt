package po.lognotify.extensions

import kotlinx.coroutines.withContext
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.createChild
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.exceptions.handleException


suspend inline fun <reified T, R: Any?> T.subTaskAsync(
    taskName: String,
    config: TaskConfig = TaskConfig(),
    noinline block: suspend  T.(TaskHandler<R>)-> R
): TaskResult<R> {
    val rootTask = TasksManaged.taskDispatcher.activeRootTask()
    val moduleName: String =  this::class.simpleName?:config.moduleName
    val result = if(rootTask != null){
        val childTask = rootTask.createChild<R>(taskName, moduleName)
        withContext(childTask.coroutineContext){
            try {
                childTask.onStart()
                val value =  block.invoke(this@subTaskAsync, childTask.handler)
                childTask.onComplete()
                TaskResult(childTask, value)
            }catch (throwable: Throwable){
                throwable.handleException(this, childTask)
            }
        }
    }else{
        runTaskAsync(taskName, config, block)
    }
    return result
}


inline fun <reified T, R: Any?> T.subTask(
    taskName: String,
    config: TaskConfig = TaskConfig(),
    block: T.(TaskHandler<R>) -> R
): TaskResult<R>{
    val rootTask = TasksManaged.taskDispatcher.activeRootTask()
    val moduleName = this::class.simpleName?:config.moduleName
    return if(rootTask != null){
        val childTask = rootTask.createChild<R>(taskName, moduleName)
        try {
           val value = block.invoke(this, childTask.handler)
            TaskResult(childTask, value)

        }catch (throwable: Throwable){
            throwable.handleException(this, childTask)
        }
    }else{
        this.runTask(taskName, config, block)
    }
}


