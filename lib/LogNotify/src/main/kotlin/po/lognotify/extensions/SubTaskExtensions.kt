package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.classes.task.ManagedTask
import po.lognotify.classes.taskresult.ManagedResult
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.models.TaskKey
import po.misc.exceptions.CoroutineInfo
import kotlin.coroutines.CoroutineContext

suspend  fun <T, R> T.subTask(
    taskName: String,
    moduleName: String? = null,
    block: suspend  T.(TaskHandler<R>)-> R
):ManagedResult<R> {

    return TasksManaged.attachToHierarchy<R>(taskName, moduleName)?.let {
        val runResult = it.runTask(this ,block)
        val casted = runResult.castOrLoggerException<ManagedResult<R>>()
        casted
    }?:run {
       return this.startTaskAsync(taskName, moduleName, block)
    }
}

//suspend  fun <T, R> T.withLastTask(
//    block: suspend  T.(TaskHandler<R>)-> R
//):R? {
//    val lastTask  = TasksManaged.continueWithLastTask<R>()
//    block.invoke(this, lastTask.taskHandler)
//    return lastTask.taskResult.resultOrNull()
//}

fun lastTaskHandler():TaskHandler<*> {
    val lastTaskHandler  = TasksManaged.getLastTaskHandler()
    return lastTaskHandler
}