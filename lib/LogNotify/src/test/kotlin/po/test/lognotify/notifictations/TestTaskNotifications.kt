package po.test.lognotify.notifictations

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.printable.json.toJson
import po.test.lognotify.setup.FakeTasksManaged
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTaskNotifications : FakeTasksManaged {


    @Test
    fun `Logger messages can be serialized to json`() {
        val dataProcessor = logHandler.logger
        dataProcessor.notify("some message")
        assertTrue(dataProcessor.records.isNotEmpty())
        val record = assertNotNull(dataProcessor.records.firstOrNull())
        val jsonString = record.toJson()
        jsonString.output()
    }

}