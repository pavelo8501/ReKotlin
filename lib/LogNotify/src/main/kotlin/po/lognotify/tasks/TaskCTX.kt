package po.lognotify.tasks

import po.lognotify.interfaces.LoggableCTX
import po.misc.interfaces.CtxId


class TaskCTX<T: CtxId, R: Any?>(
    private val sourceTask: TaskBase<T, R>
): LoggableCTX<T, R> {
    override val task: TaskHandler<R> get() = sourceTask.handler
    override val ctx:T get() = sourceTask.ctx
}

fun <T:CtxId, R:Any?>  TaskBase<T, R>.containerize():TaskCTX<T, R>{
   return TaskCTX(this)
}