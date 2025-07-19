package po.lognotify.tasks.models

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import po.misc.coroutines.LauncherType
import po.misc.exceptions.HandlerType


enum class TaskOptions{
    RunNested,
    RunAsRootTask
}


data class TaskConfig (
    var attempts: Int = 0,
    var delayMs: Long = 2000,
    var moduleName: String = "",
    var options:TaskOptions = TaskOptions.RunNested,
    var initiator: String? = null,
    var exceptionHandler: HandlerType = HandlerType.SkipSelf,
    var dispatcher: CoroutineDispatcher = Dispatchers.Default,
    var launcherType: LauncherType = LauncherType.AsyncLauncher,
    @PublishedApi internal val isDefault: Boolean = false
)
