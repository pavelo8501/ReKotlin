package po.test.lognotify.setup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import po.lognotify.TasksManaged
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.dispatcher.createHierarchyRoot
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.createTask
import po.misc.context.CTX
import po.misc.context.CTXIdentity

import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel

/**
 * A singleton test context used to simulate a real [CTX] in unit or integration tests.
 * This object provides a fixed [identity] based on `asContext()` for mocking or stubbing
 * systems that rely on context-based identity resolution.
 *
 * @see CTX
 * @see CTXIdentity
 */
internal object FakeContext: CTX{
    override val identity = asIdentity()
}

/**
 * A test-only interface for mocking [TasksManaged] implementations using a fixed [FakeContext].
 *
 * This interface can be mixed into test classes to automatically fulfill the identity requirement
 * for systems built around [TasksManaged], without needing to construct a full real context.
 *
 * @see TasksManaged
 * @see FakeContext
 */
internal interface FakeTasksManaged : TasksManaged {

    override val identity: CTXIdentity<FakeContext> get() = FakeContext.identity
    val mockedDispatcher get() = TasksManaged.LogNotify.taskDispatcher
    val mockScope: CoroutineScope get() =  CoroutineScope(Dispatchers.Default)

    fun <T: FakeTasksManaged> T.mockRootTask(name: String = "root_task"): RootTask<T, Unit>{
        return createHierarchyRoot(name, this, TaskConfig())
    }

    fun  <T: FakeTasksManaged> TaskBase<T, *>.mockChildTask(name: String, config: TaskConfig = TaskConfig()): TaskBase<T, Unit>{
       return  createTask(name, this.receiver, config)
    }
}