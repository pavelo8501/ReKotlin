package po.test.lognotify.process

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.launchers.runProcess
import po.lognotify.process.Process
import po.test.lognotify.setup.FakeTasksManaged
import po.test.lognotify.setup.newMockedSession
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestProcessFunctionality: FakeTasksManaged {


    @Test
    fun `After process completes all resources are freed`() = runTest{
        var process: Process<*>? = null
        runProcess(newMockedSession){
            process = handler.process
            mockRootTask()
        }
        delay(100)
        val loggerProcess = assertNotNull(process)
        assertEquals(0, loggerProcess.dataNotifier.subscriptionsCount)
        assertEquals(0, mockedDispatcher.processRegistry.size)
        assertNull(mockedDispatcher.activeProcess())
    }

    @Test
    fun `Process receives and passes data to session`() = runTest{
        var dataFromProcess: Any? = null
        val session = newMockedSession
        runProcess(session){
            onDataReceived{
                dataFromProcess = it
            }
            mockRootTask()
        }
        delay(100)
        assertNotNull(dataFromProcess)
        val dataInSession = assertNotNull(session.extractLogRecords().firstOrNull())
        assertEquals(dataFromProcess, dataInSession)
    }


}