package po.lognotify.classes.task.models

import po.lognotify.models.TaskKey
import po.misc.exceptions.CoroutineInfo



//
//data class TaskData(
//    val taskKey: TaskKey,
//    val coroutineInfo: CoroutineInfo,
//){
//    val taskName: String  get() = taskKey.taskName
//    val nestingLevel: Int get() = taskKey.nestingLevel
//    val moduleName: String get() = taskKey.moduleName?:"N/A"
//
//}