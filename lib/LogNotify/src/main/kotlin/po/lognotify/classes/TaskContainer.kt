package po.lognotify.classes

import po.lognotify.interfaces.LoggableCTX
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.TaskCTX
import po.misc.interfaces.CtxId
import po.misc.types.containers.context.CTXContainer

class TaskContainer<T: CtxId, R: Any?>(
    private val sourceTask: TaskBase<T, R>,
    taskContext: TaskCTX<T, R>
): CTXContainer<TaskBase<T, R>>(sourceTask), LoggableCTX<T, R> by taskContext{


    companion object {
        fun <T: CtxId, R: Any?> create(task: TaskBase<T, R>): TaskContainer<T,R>{
            return TaskContainer(task, TaskCTX(task))
        }
    }
}
