package po.test.lognotify.notifictations

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.launchers.runTask
import po.lognotify.tasks.TaskHandler
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestArbitraryDataProcessing {

    internal class SomeComponent : TasksManaged {
        override val identity: CTXIdentity<SomeComponent> = asIdentity()
        fun runAndWarn(text: String): TaskHandler<*> = runTask("runAndEmmit") {
            notify(text, SeverityLevel.WARNING)
            taskHandler
        }.resultOrException()
    }

    @Test
    fun `Third components can use loggers message bus`() {
        val inputMessage = "Warning"
        val component1 = SomeComponent()
        val handler: TaskHandler<*> = component1.runAndWarn(inputMessage)
        assertTrue(handler.dataProcessor.records.isNotEmpty())
        val message = handler.dataProcessor.records
            .flatMap { it.events.records }
            .firstOrNull { it.severity == SeverityLevel.WARNING }

        val warningMessage =  assertNotNull(message)
        assertEquals(inputMessage, warningMessage.message)
    }
}