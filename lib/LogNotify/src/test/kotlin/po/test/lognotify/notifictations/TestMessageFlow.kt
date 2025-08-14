package po.test.lognotify.notifictations

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.common.configuration.TaskConfig
import po.test.lognotify.setup.FakeTasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.notification.models.LogData
import po.lognotify.tasks.info
import po.misc.data.processors.SeverityLevel
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestMessageFlow : FakeTasksManaged {

    val listenerScope = CoroutineScope(CoroutineName("Listener"))

    @Test
    fun `Events propagation chain work as expected`() {
        val loggedMessage = "Logged in ChildTask"
        val config = TaskConfig()
        config.setConsoleBehaviour(ConsoleBehaviour.Mute)
        val topTask = mockRootTask("TopTask")
        val childTask1 = topTask.mockChildTask("ChildTask1")
        val childTask2 = childTask1.mockChildTask("ChildTask2")

        childTask2.info(loggedMessage)

        assertEquals(3, topTask.dataProcessor.records.size)
        assertIs<LogData>(topTask.dataProcessor.records[0])
        assertIs<LogData>(topTask.dataProcessor.records[1])
        assertIs<LogData>(topTask.dataProcessor.records[2])

        assertEquals(2, childTask1.dataProcessor.records.size)
        assertIs<LogData>(childTask1.dataProcessor.records[0])
        assertIs<LogData>(childTask1.dataProcessor.records[1])

        assertEquals(1, childTask2.dataProcessor.records.size)
        assertIs<LogData>(childTask2.dataProcessor.records[0])

        val record = assertNotNull(topTask.dataProcessor.records.lastOrNull())
        val childRecord = assertNotNull(record.events.records.firstOrNull { it.severity == SeverityLevel.INFO })
        assertEquals(loggedMessage, childRecord.message)
    }


    @Test
    fun `Task emitter propagate events properly`() = runTest {

        val collectedData = mutableListOf<LogData>()
        val rootTask = mockRootTask()
        val emitter = assertNotNull( rootTask.dataProcessor.flowEmitter)
        emitter.collectEmissions(listenerScope){
            collectedData.add(it)
        }
        val task1 = rootTask.mockChildTask("Task1")
        delay(100)
        val task2 = task1.mockChildTask("Task2")
        delay(100)
        assertEquals(collectedData.size, 3)
    }
}
