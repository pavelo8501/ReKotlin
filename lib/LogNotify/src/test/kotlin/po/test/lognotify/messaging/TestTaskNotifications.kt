package po.test.lognotify.messaging

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.test.lognotify.setup.FakeTasksManaged
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.json.toJson
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTaskNotifications : FakeTasksManaged {


    @Test
    fun `Logger messages can be serialized to json`() {
        val notifications: MutableList<PrintableBase<*>> = mutableListOf()

        val dataProcessor = logHandler.logger

        dataProcessor.notify("some message")

        assertTrue(dataProcessor.records.isNotEmpty())
        val record = assertNotNull(dataProcessor.records.firstOrNull())
        val jsonString = record.toJson()
        jsonString.output()

    }

}
