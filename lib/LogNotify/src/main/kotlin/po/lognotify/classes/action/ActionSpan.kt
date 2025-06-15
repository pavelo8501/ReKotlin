package po.lognotify.classes.action

import po.lognotify.models.TaskKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.time.ExecutionTimeStamp

class ActionSpan<T,R>(
    val identifiable: IdentifiableContext,
    val actionName: String,
    val inTask: TaskKey,
    val ctx: T,
    val result:R
) {

 val executionTime: ExecutionTimeStamp = ExecutionTimeStamp(identifiable.contextName, actionName)

}