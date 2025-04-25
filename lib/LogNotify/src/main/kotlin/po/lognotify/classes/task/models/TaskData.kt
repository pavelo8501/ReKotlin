package po.lognotify.classes.task.models

import po.lognotify.classes.task.TaskIdentification
import po.lognotify.models.TaskKey
import kotlin.coroutines.CoroutineContext

data class TaskData(
    val taskKey: TaskKey,
    override val qualifiedName: String,
    override val startTime: Long,
    override var endTime: Long,
    override val coroutineContext: CoroutineContext
) : TaskIdentification{


    override val taskName: String  get() = taskKey.taskName
    override val nestingLevel: Int get() = taskKey.nestingLevel
    override val moduleName: String get() = taskKey.moduleName?:"N/A"


}