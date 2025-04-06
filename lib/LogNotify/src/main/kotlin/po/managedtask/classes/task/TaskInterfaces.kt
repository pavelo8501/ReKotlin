package po.managedtask.classes.task

import po.managedtask.exceptions.SelfThrownException
import po.managedtask.models.TaskKey

interface ControlledTask  {
    val parent: TaskSealedBase<*>
    val key : TaskKey
    fun  propagateToParent(th: Throwable)
}

interface ResultantTask : SelfThrownException  {
    val taskName: String
}