package po.test.exposify.scope.connection

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.result.toResultSingle
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.lognotify.TasksManaged
import po.lognotify.launchers.runProcess
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.mocks.mockedSession
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class TestCoroutineEmitter: DatabaseTest(), TasksManaged  {

    override val identity: CTXIdentity<TestCoroutineEmitter> =  asIdentity()

    @Test
    fun `Emitter uses correct coroutine context`(): TestResult = runTest{

        var session: AuthorizedSession? = null
        with(mockedSession){
            runProcess(this){
                val emitter = connectionClass.requestEmitter(it)
                assertSame(mockedSession, emitter.process.receiver)
                emitter.dispatchSingle {
                    session = coroutineContext[AuthorizedSession]
                    operationsException("TestCase", ExceptionCode.INVALID_DATA).toResultSingle(PageDTO)
                }
            }
            runProcess(this) {
                val emitter = connectionClass.requestEmitter(it)
                assertSame(mockedSession, emitter.process.receiver)
                emitter.dispatchSingle {
                    session = coroutineContext[AuthorizedSession]
                    operationsException("TestCase", ExceptionCode.INVALID_DATA).toResultSingle(PageDTO)
                }
            }
        }
        assertNotNull(session)
    }
}