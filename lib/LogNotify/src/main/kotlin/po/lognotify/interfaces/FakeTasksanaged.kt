package po.lognotify.interfaces

import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asContext

/**
 * A singleton test context used to simulate a real [CTX] in unit or integration tests.
 * This object provides a fixed [identity] based on `asContext()` for mocking or stubbing
 * systems that rely on context-based identity resolution.
 *
 * @see CTX
 * @see CTXIdentity
 */
object FakeContext: CTX{
    override val identity = asContext()
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
}