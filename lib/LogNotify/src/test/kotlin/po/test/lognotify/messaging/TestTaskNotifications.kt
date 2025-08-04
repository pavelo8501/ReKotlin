package po.test.lognotify.messaging

import org.junit.jupiter.api.Test
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.launchers.runTask
import po.test.lognotify.setup.FakeTasksManaged
import po.lognotify.notification.NotifierHub
import po.lognotify.notification.models.LogData
import po.misc.data.printable.PrintableBase
import po.misc.data.processors.SeverityLevel
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestTaskNotifications : FakeTasksManaged {
    @Test
    fun `Debug information can be switched on&off for specific data type`() {
        val notifications: MutableList<PrintableBase<*>> = mutableListOf()

        logHandler.notifierHub.subscribe(this, NotifierHub.Event.DataReceived) {
            notifications.add(it)
        }
        runTask("RootTaskNoDebug") {
        }
        assertFalse(notifications.any { (it as LogData).overallSeverity == SeverityLevel.DEBUG }, "Some of notifications are of type DEBUG")

        notifications.clear()

        logHandler.notifierConfig {
            allowDebug(LogData)
        }
        runTask("RootTaskWithDebug") {
        }
        assertTrue(notifications.any { (it as LogData).overallSeverity == SeverityLevel.DEBUG }, "None of the notifications are of type DEBUG")
    }

    fun `Exception info displayed correctly lambdas expect NonNullable`() {
        fun subTask1(): Int =
            runTask("Sub task 1") {
                throw Exception("GenericException")
                10
            }.resultOrException()

        runTask("Task1", TaskConfig(initiator = "TestInstance")) {
            subTask1()
            println("End of subTask1")
        }
    }

    fun subTask2(): Int =
        runTask("Sub task 2") {
            throw Exception("GenericException")
            10
        }.resultOrException()

    fun `Exception info displayed correctly lambdas expect Unit`() {
        runTask("Task1", TaskConfig(initiator = "TestInstance")) {
            runTask("Sub task 1") {
                subTask2()
            }.onFail {
                val exception = it
            }
            println("End of Task1 lambda reached")
        }
        println("End of Exception info displayed correctly test reached")
    }

    fun `Task predefined messages work as expected`() {
        runTask("Task1", TaskConfig(initiator = "TestInstance")) {
            val stopEvent = taskHandler.dataProcessor.registerStop()
            val info = taskHandler.notify("Info message")
            val warning = taskHandler.notify("Info message")
        }
    }
}
