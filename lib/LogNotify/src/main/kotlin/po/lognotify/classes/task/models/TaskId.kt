package po.lognotify.classes.task.models

import po.misc.coroutines.CoroutineInfo


data class TaskId(
    val id : Int = 0,
    val name: String= "",
    val nestingLevel: Int = 0,
    val moduleName: String = "",
    val coroutineInfo : CoroutineInfo = CoroutineInfo("",0,"","",0,emptyList())
)