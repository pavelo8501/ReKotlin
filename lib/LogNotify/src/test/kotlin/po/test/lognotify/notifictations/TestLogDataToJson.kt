package po.test.lognotify.notifictations

import org.junit.jupiter.api.Test
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.launchers.runTaskBlocking
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.toJson
import po.lognotify.tasks.createTask
import po.test.lognotify.setup.FakeTasksManaged
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestLogDataToJson: FakeTasksManaged {

    @Test
    fun `Log data to json conversion`() {

        var processor: LoggerDataProcessor? = null
        runTaskBlocking("SomeTask") {
            processor = taskHandler.dataProcessor
            val task = taskHandler.task.createTask<TestLogDataToJson, Unit>("SomeTask", this, TaskConfig())
            task.notify("Something")
        }
        val someTaskDataProcessor = assertNotNull(processor)
        assertTrue(someTaskDataProcessor.records.isNotEmpty())
        assertEquals(2, someTaskDataProcessor.records.size)
        val jsonStr = someTaskDataProcessor.records.toJson()
        assertTrue(jsonStr.isNotEmpty())
        assertTrue(jsonStr.contains("Something"))
    }

}