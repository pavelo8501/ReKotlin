package po.lognotify.classes.task.models


data class TaskSettings(
    var attempts: Int = 1,
    var delayMs: Long = 2000,
    var moduleName: String = "",
)
