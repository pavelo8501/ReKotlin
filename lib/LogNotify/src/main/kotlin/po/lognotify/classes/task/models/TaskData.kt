package po.lognotify.classes.task.models

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import po.misc.coroutines.LauncherType

data class TaskConfig (
    var attempts: Int = 1,
    var delayMs: Long = 2000,
    var moduleName: String = "",
    var actor: String? = null,
    var dispatcher: CoroutineDispatcher = Dispatchers.Default,
    var launcherType: LauncherType = LauncherType.AsyncLauncher
)
