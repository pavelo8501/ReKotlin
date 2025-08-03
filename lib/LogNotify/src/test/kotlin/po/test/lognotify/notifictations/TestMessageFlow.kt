package po.test.lognotify.notifictations

import org.junit.jupiter.api.Test
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.interfaces.FakeTasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.createChild
import po.misc.data.processors.SeverityLevel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestMessageFlow : FakeTasksManaged {
    @Test
    fun `Events propagation chain work as expected`() {
        val loggedMessage = "Logged in ChildTask"
        val config = TaskConfig()
        config.setConsoleBehaviour(ConsoleBehaviour.Mute)
        val topTask: RootTask<TestMessageFlow, Unit> = dispatcher.createHierarchyRoot("TopTask", "TestDataProcessor", this, config)
        val topTaskProcessor = topTask.dataProcessor

        val subTask = topTask.createChild<TestMessageFlow, Unit>("ChildTask1", this)
        val subTaskProcessor = subTask.dataProcessor

        val subSubTask = subTask.createChild<TestMessageFlow, Unit>("ChildTask2", this)
        val subSubTaskProcessor = subSubTask.dataProcessor
        subSubTask.notify(loggedMessage, SeverityLevel.INFO)

        subSubTask.complete()
        subTask.complete()
        topTask.complete()

        assertEquals(1, subSubTaskProcessor.recordsCount)
        assertEquals(2, subTaskProcessor.recordsCount)
        assertEquals(3, topTaskProcessor.recordsCount)
        val record = assertNotNull(topTaskProcessor.records.lastOrNull())
        val childRecord = assertNotNull(record.events.records.firstOrNull { it.severity == SeverityLevel.INFO })
        assertEquals(loggedMessage, childRecord.message)
    }
}
