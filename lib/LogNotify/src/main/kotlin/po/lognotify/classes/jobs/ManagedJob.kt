package po.lognotify.classes.jobs

import po.lognotify.classes.task.ControlledTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.runner.TaskRunner

class ManagedJob(
    val task : ControlledTask,
) {



    suspend inline fun <T, R, R2> startJob(
        taskHandler: TaskHandler<R2>,
        receiver:T,
        crossinline  block: suspend T.()->R){

        val taskRunner = TaskRunner<R2>(task, taskHandler)
        taskRunner.executeJob(receiver, block)

    }


}