package po.lognotify.extensions

import kotlinx.coroutines.withContext
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.createChild
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.result.createFaultyResult
import po.lognotify.classes.task.result.onTaskResult
import po.lognotify.exceptions.handleException


suspend inline fun <reified T, R: Any?> T.subTaskAsync(
    taskName: String,
    config: TaskConfig = TaskConfig(),
    noinline block: suspend  T.(TaskHandler<R>)-> R
): TaskResult<R> {
    val rootTask = TasksManaged.taskDispatcher.activeRootTask()
    val moduleName: String =  this::class.simpleName?:config.moduleName
    val result = if(rootTask != null){
        val subTask = rootTask.createChild<T, R>(taskName, moduleName, config, this)
        withContext(subTask.coroutineContext){
            try {
                subTask.start()
                val value =  block.invoke(this@subTaskAsync, subTask.handler)
                onTaskResult<T, R>(subTask, value)
                TaskResult(subTask, value)
            }catch (throwable: Throwable){
              val managed =  handleException(throwable, subTask)
              createFaultyResult(managed, subTask)
            }finally {
                subTask.complete()
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
   crossinline block: T.(TaskHandler<R>) -> R
): TaskResult<R>{
    val rootTask = TasksManaged.taskDispatcher.activeRootTask()
    val moduleName = this::class.simpleName?:config.moduleName
    val result =  rootTask?.let {
       val subTask = it.createChild<T, R>(taskName, moduleName, config, this)
        subTask.start()
       val subTaskResult = try {
            val value = block.invoke(this, subTask.handler)
            onTaskResult<T,R>(subTask, value)
        }catch (throwable: Throwable) {
           val managed = handleException(throwable, subTask)
           createFaultyResult(managed, subTask)
        } finally {
            subTask.complete()
        }
        subTaskResult
    }?:this.runTask(taskName, config, block)
    return result
}


