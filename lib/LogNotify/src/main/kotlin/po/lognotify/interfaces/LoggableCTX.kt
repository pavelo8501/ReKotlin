package po.lognotify.interfaces

import po.lognotify.tasks.TaskHandler
import po.misc.interfaces.CtxId
import po.misc.interfaces.ManagedContext

interface LoggableCTX<T: CtxId, R: Any?>: ManagedContext {
    val task: TaskHandler<R>
    val ctx:T
}