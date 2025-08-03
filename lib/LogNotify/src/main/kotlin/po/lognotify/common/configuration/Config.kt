package po.lognotify.common.configuration

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.notification.models.NotifyConfig
import po.lognotify.tasks.TaskBase
import po.misc.coroutines.LauncherType
import po.misc.exceptions.HandlerType

enum class TaskType {
    Nested,
    AsRootTask,
}

class LaunchOptions(
    @PublishedApi
    internal var launcherType: LauncherType = LauncherType.AsyncLauncher,
    @PublishedApi
    internal var coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
    internal val forConfig: TaskConfig,
) {
    internal var taskOptions: TaskType = TaskType.Nested

    fun setDispatcher(dispatcher: CoroutineDispatcher): TaskConfig {
        coroutineDispatcher = dispatcher
        return forConfig
    }

    fun setLauncherType(type: LauncherType): TaskConfig {
        launcherType = type
        return forConfig
    }

    fun setTaskType(options: TaskType): TaskConfig {
        taskOptions = options
        return forConfig
    }
}

data class TaskConfig(
    override var attempts: Int = 0,
    override var delayMs: Long = 2000,
    var initiator: String? = null,
    var exceptionHandler: HandlerType = HandlerType.SkipSelf,
    var taskType: TaskType = TaskType.Nested,
    @PublishedApi internal val isDefault: Boolean = false,
) : CommonConfiguration {
    internal val notifConfig: NotifyConfig = NotifyConfig()

    fun setConsoleBehaviour(consoleBehaviour: ConsoleBehaviour) {
        notifConfig.console = consoleBehaviour
    }

    @PublishedApi
    internal val launchOptions: LaunchOptions = LaunchOptions(forConfig = this)

    fun launchOptions(builder: LaunchOptions.() -> TaskConfig): TaskConfig = builder(launchOptions)

    fun onFaultRepeatAttempts(count: Int): TaskConfig {
        attempts = count
        return this
    }

    fun onFaultDelay(delayMs: Long): TaskConfig {
        this.delayMs = delayMs
        return this
    }

    fun setExceptionHandler(handler: HandlerType): TaskConfig {
        exceptionHandler = handler
        return this
    }
}

data class SpanConfig(
    override var attempts: Int = 0,
    override var delayMs: Long = 2000,
) : CommonConfiguration
