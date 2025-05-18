package po.lognotify.classes.jobs

import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskBase

class ManagedJob(
    val task : TaskBase<*>,
) {
    suspend inline fun <T, R, R2> startJob(
        taskHandler: TaskHandler<R2>,
        receiver:T,
        crossinline  block: suspend T.()->R){
       // val taskRunner = TaskRunner<R2>(task, taskHandler)
      //  taskRunner.executeJob(receiver, block)
    }
}