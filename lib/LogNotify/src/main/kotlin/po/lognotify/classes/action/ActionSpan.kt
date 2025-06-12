package po.lognotify.classes.action

import po.lognotify.models.TaskKey
import po.misc.interfaces.Identifiable
import po.misc.time.ExecutionTimeStamp

class ActionSpan<T,R>(
    val identifiable: Identifiable,
    val actionName: String,
    val inTask: TaskKey,
    val ctx: T,
    val result:R
) {

 val executionTime: ExecutionTimeStamp = ExecutionTimeStamp(identifiable.componentName.toString(), identifiable.sourceName)

}