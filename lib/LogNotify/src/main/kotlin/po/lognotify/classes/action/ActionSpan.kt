package po.lognotify.classes.action

import po.lognotify.TaskProcessor
import po.lognotify.models.TaskKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.time.ExecutionTimeStamp

class ActionSpan<T>(
    val actionName: String,
    val inTask: TaskKey,
    val ctx: T,
): TaskProcessor where T: InlineAction {

 val executionTime: ExecutionTimeStamp = ExecutionTimeStamp(ctx.contextName, actionName)

}