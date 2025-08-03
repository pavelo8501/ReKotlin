package po.test.lognotify.notifictations

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.lognotify.action.ActionSpan
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.interfaces.FakeTasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.tasks.RootTask
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.test.lognotify.setup.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDataProcessor : FakeTasksManaged {
    companion object : FakeTasksManaged {
        @JvmStatic
        val rootTask: RootTask<Companion, Unit> =
            dispatcher.createHierarchyRoot<Companion, Unit>("TestTask", "TestDataProcessor", this)

        @JvmStatic
        internal val processor = rootTask.dataProcessor
    }

    private fun exceptionMethod(message: String): ManagedException {
        val payload = ManagedPayload(message, "exceptionMethod", this)
        return ManagedException(payload)
    }

    @Test
    fun `Console printouts can be muted by setup`() {
        val config = TaskConfig()
        config.setConsoleBehaviour(ConsoleBehaviour.Mute)

        val fullMuteOutput =
            captureOutput {
                dispatcher.createHierarchyRoot<TestDataProcessor, Unit>("MutedEventsTask", "TestDataProcessor", this, config)
            }
        assertTrue(fullMuteOutput.isEmpty())

        config.setConsoleBehaviour(ConsoleBehaviour.MuteNoEvents)
        val muteNoEventsSilent =
            captureOutput {
                dispatcher.createHierarchyRoot<TestDataProcessor, Unit>("MutedEventsTask", "TestDataProcessor", this, config)
            }
        assertTrue(muteNoEventsSilent.isEmpty())

        val muteNoEventsTask: RootTask<TestDataProcessor, Unit> =
            dispatcher.createHierarchyRoot("MuteNoEventsTask", "TestDataProcessor", this, config)
        val processor = muteNoEventsTask.dataProcessor

        var output =
            captureOutput {
                processor.notify("UnmuteEvent", SeverityLevel.INFO, muteNoEventsTask)
            }
        assertTrue(output.contains("MuteNoEventsTask") && output.contains("UnmuteEvent"))

        config.setConsoleBehaviour(ConsoleBehaviour.MuteInfo)
        val muteInfoTask: RootTask<TestDataProcessor, Unit> =
            dispatcher.createHierarchyRoot("MutedInfoTask", "TestDataProcessor", this, config)
        output =
            captureOutput {
                muteInfoTask.dataProcessor.notify("MutedInfo", SeverityLevel.INFO, muteInfoTask)
            }
        assertTrue(output.isEmpty())
        assertEquals(1, muteInfoTask.dataProcessor.taskEvents.records.size)

        output =
            captureOutput {
                muteInfoTask.dataProcessor.notify("UnMutableWarning", SeverityLevel.WARNING, muteInfoTask)
            }
        assertTrue(output.contains("UnMutableWarning"))
    }

    @Test
    fun `TaskEvents(group) saves LogEvents as expected`() {
        val taskGroup = processor.taskEvents
        taskGroup.clear()
        processor.notify("Info message", SeverityLevel.INFO, rootTask)
        processor.notify("Warning message", SeverityLevel.WARNING, rootTask)
        assertTrue(taskGroup.groupHost.taskHeader.isNotEmpty())
        assertTrue(taskGroup.groupHost.taskFooter.isNotEmpty())
        assertEquals(2, taskGroup.records.size)
    }

    @Test
    fun `TaskEvents(group) saves LogEvents from all logger instances`() {
        val actionSpan = rootTask.createActionSpan<Companion, Unit>("TestTask", TestDataProcessor)
        val taskGroup = processor.taskEvents
        processor.notify("Info message", SeverityLevel.INFO, rootTask)
        processor.notify("ActionSpan message", SeverityLevel.INFO, actionSpan)
        assertTrue(taskGroup.records.size == 2)
    }
}
