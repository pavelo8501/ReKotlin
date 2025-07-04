package po.lognotify.extensions

import kotlinx.coroutines.withContext
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.createChild
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.result.createFaultyResult
import po.lognotify.classes.task.result.onTaskResult
import po.lognotify.exceptions.handleException
import po.misc.exceptions.HandlerType
import po.misc.interfaces.IdentifiableContext
import po.misc.reflection.properties.takePropertySnapshot


suspend inline fun <reified T: Any, R: Any?> T.subTaskAsync(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true),
    noinline block: suspend  T.(TaskHandler<R>)-> R
): TaskResult<R> {


    var effectiveConfig = config
    val rootTask = TasksManaged.LogNotify.taskDispatcher.activeRootTask()
    if(rootTask != null && config.isDefault){
        effectiveConfig = rootTask.config
    }
    val moduleName: String =  this::class.simpleName?:config.moduleName
    val result = if(rootTask != null){
        val subTask = rootTask.createChild<T, R>(taskName, moduleName, effectiveConfig, this)
        withContext(subTask.coroutineContext){
            try {
                subTask.start()
                val value =  block.invoke(this@subTaskAsync, subTask.handler)
                onTaskResult<T, R>(subTask, value)
                TaskResult(subTask, value)
            }catch (throwable: Throwable){
              val managed =  handleException(throwable, subTask, null)
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


inline fun <reified T: IdentifiableContext, R: Any?> T.subTask(
    taskName: String,
    config: TaskConfig = TaskConfig(isDefault = true),
    block: T.(TaskHandler<R>) -> R
): TaskResult<R>{

    var effectiveConfig = config
    val rootTask = TasksManaged.LogNotify.taskDispatcher.activeRootTask()
    if(rootTask != null && config.isDefault){
        effectiveConfig = rootTask.config
    }

    val moduleName = this::class.simpleName?:config.moduleName
    val result =  rootTask?.let {
       val subTask = it.createChild<T, R>(taskName, moduleName, effectiveConfig, this)
        subTask.start()
       val subTaskResult = try {
            val value = block.invoke(this, subTask.handler)
            onTaskResult<T,R>(subTask, value)
        }catch (throwable: Throwable) {
           val snapshot = takePropertySnapshot<T, LogOnFault>(this)
           val managed = handleException(throwable, subTask, snapshot)
           val subtaskFaultyResult =  if(managed.handler == HandlerType.CancelAll){
               rootTask.complete(managed)
           }else{
               createFaultyResult(managed, subTask)
           }
           subtaskFaultyResult
        } finally {
            subTask.complete()
        }
        subTaskResult
    }?:this.runTask(taskName, config, block)
    return result
}


