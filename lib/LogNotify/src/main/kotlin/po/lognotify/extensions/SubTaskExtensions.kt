package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskResult


fun <T, R> T.subTaskBlocking(
    taskName: String,
    moduleName: String? = null,
    block: T.(TaskHandler<R>)-> R
){

    val rootTask = TasksManaged.taskDispatcher.lastRootTask()
    return if(rootTask != null && rootTask is RootTask){
       // rootTask.createChildTask<R>(taskName, rootTask, moduleName ?: rootTask.key.moduleName).runTask(this, block)
    }else{
      //  this.newTaskAsync(taskName, moduleName ?: "N/A", block)
    }
}



suspend  fun <T, R> T.subTask(
    taskName: String,
    moduleName: String? = null,
    block: suspend  T.(TaskHandler<R>)-> R
): TaskResult<R> {

    val rootTask = TasksManaged.taskDispatcher.lastRootTask()
    return if(rootTask != null && rootTask is RootTask){
        rootTask.createChildTask<R>(taskName, rootTask, moduleName ?: rootTask.key.moduleName).runTask(this, block)
    }else{
        this.newTaskAsync(taskName, moduleName ?: "N/A", TaskSettings(), block)
    }
}
