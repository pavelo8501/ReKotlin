package po.test.lognotify.process

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.auth.extensions.session
import po.auth.sessions.interfaces.SessionIdentified
import po.lognotify.TasksManaged
import po.lognotify.extensions.runTask
import po.lognotify.process.runProcess
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asContext

class TestProcessFlow() : TasksManaged {

    override val identity: CTXIdentity<TestProcessFlow> = asContext()

    override val contextName: String = "TestProcessFlow"

    class SessionIdentity(override val sessionID: String, override val remoteAddress: String): SessionIdentified

    fun rootTask() = runTask("RootTask"){

    }

    @Test
    fun `Log flow completes with process completion`() = runTest {

        val session = session(SessionIdentity("0", "192.169.1.1"))
        session.runProcess("TestProcess", Dispatchers.Default){
            rootTask()
        }

        val tasks = logHandler.dispatcher.getTasks()
        tasks.forEach {
            println(it)
        }
    }

}