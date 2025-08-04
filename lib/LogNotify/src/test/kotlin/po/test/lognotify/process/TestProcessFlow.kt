package po.test.lognotify.process

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.auth.sessions.models.AuthorizedSession
import po.test.lognotify.setup.FakeTasksManaged
import po.lognotify.launchers.runProcess
import po.lognotify.launchers.runTaskAsync
import po.lognotify.notification.models.LogData
import po.misc.coroutines.coroutineInfo
import po.misc.data.printable.PrintableBase
import po.test.lognotify.setup.mockedSession
import kotlin.test.assertIs
import kotlin.test.assertNotNull

import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestProcessFlow : FakeTasksManaged {

    @Test
    fun `Process launcher keeps reference to original session object`(): TestResult = runTest {
        mockedSession.runProcess(AuthorizedSession) {
            println(coroutineContext.coroutineInfo())
            val session = assertNotNull(coroutineContext[AuthorizedSession])
            assertSame(mockedSession, session)
        }

        runProcess(mockedSession, AuthorizedSession) {
            val session = assertNotNull(coroutineContext[AuthorizedSession])
            assertSame(mockedSession, session)
        }
    }

    @Test
    fun `Process receives updates from underlying tasks and forward to receiver`(): TestResult = runTest {
        val dataReceived: MutableList<PrintableBase<*>> = mutableListOf()
        runProcess(mockedSession, AuthorizedSession) {

            onDataReceived { dataReceived.add(it) }
            runTaskAsync("Task1"){

            }
        }
        assertTrue(dataReceived.isNotEmpty())
        val taskData =  assertIs<LogData>(dataReceived[0])
        assertTrue(taskData.taskHeader.contains("Task1"))
        assertTrue(mockedSession.logRecords.isNotEmpty())
        assertSame(taskData, mockedSession.logRecords[0])
        taskData.echo()
    }
}