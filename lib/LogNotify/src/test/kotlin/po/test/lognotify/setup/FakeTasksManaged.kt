package po.test.lognotify.setup

import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.models.TaskDispatcher
import po.lognotify.notification.NotifierHub
import po.misc.context.CTX
import po.misc.context.CTXIdentity

import po.misc.context.asIdentity
import po.misc.data.printable.PrintableBase
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

    override val messageLogger: (String, SeverityLevel, Any) -> Unit get() = {message, severity, context->
        logHandler.logger.notify(message, severity, context)
    }
    override val datLogger: (PrintableBase<*>, SeverityLevel, Any) -> Unit get() = {printable, severity, context->
        logHandler.logger.log(printable, severity, context)
    }

}