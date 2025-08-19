package po.test.lognotify.process

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.auth.sessions.models.AuthorizedSession
import po.test.lognotify.setup.FakeTasksManaged
import po.lognotify.launchers.runProcess
import po.lognotify.launchers.runTaskAsync
import po.lognotify.launchers.runTaskBlocking
import po.lognotify.notification.models.LogData
import po.misc.coroutines.coroutineInfo
import po.misc.data.printable.PrintableBase
import po.test.lognotify.setup.mockedSession
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestProcessFlow : FakeTasksManaged {

    @Test
    fun `Process receives updates from underlying tasks and forward to receiver`(): TestResult = runTest {
        val dataReceived: MutableList<PrintableBase<*>> = mutableListOf()
        runProcess(mockedSession) {

            onDataReceived{ dataReceived.add(it) }
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

    @Test
    fun `Process coroutine elements delegated get work as expected`(): TestResult = runTest {

        var element: Any? = null
        runProcess(mockedSession){
            element =  getCoroutineElement(AuthorizedSession)
        }
        val session = assertNotNull(element)
        assertIs<AuthorizedSession>(session)
    }

    @Test
    fun `All tasks share same context`(): TestResult = runTest {

        var processCoroutineName: CoroutineName? = null
        var blockingCoroutineName: CoroutineName? = null
        var asyncCoroutineName: CoroutineName? = null

        runProcess(mockedSession){
            processCoroutineName = currentCoroutineContext()[CoroutineName]

            runTaskBlocking("Blocking task"){
                blockingCoroutineName = currentCoroutineContext()[CoroutineName]
                runTaskAsync("AsyncTask"){
                    asyncCoroutineName = currentCoroutineContext()[CoroutineName]
                }
            }
        }

        val procName = assertNotNull(processCoroutineName)
        val blockName = assertNotNull(blockingCoroutineName)
        val asyncName = assertNotNull(asyncCoroutineName)
        assertEquals(procName, blockName)
        assertEquals(blockName, asyncName)
    }


}