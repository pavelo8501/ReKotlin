package po.test.lognotify.messaging

import org.junit.jupiter.api.Test
import po.lognotify.notification.NotifierHub
import po.lognotify.notification.models.TaskData
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.enums.SeverityLevel
import po.lognotify.extensions.runTask
import po.lognotify.interfaces.FakeTasksManaged
import po.misc.data.printable.PrintableBase
import po.misc.exceptions.ManagedException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestTaskNotifications : FakeTasksManaged {


    @Test
    fun `Debug information can be switched on&off for specific data type`(){

        val notifications: MutableList<PrintableBase<*>> = mutableListOf()

       logHandler.notifierHub.subscribe(this, NotifierHub.Event.DataReceived){
            notifications.add(it)
        }
        runTask("RootTaskNoDebug"){

        }
        assertFalse(notifications.any { (it as TaskData).severity == SeverityLevel.DEBUG }, "Some of notifications are of type DEBUG")
        notifications.clear()

        logHandler.notifierConfig {
            allowDebug(TaskData)
        }
        runTask("RootTaskWithDebug"){

        }
        assertTrue(notifications.any { (it as TaskData).severity == SeverityLevel.DEBUG }, "None of the notifications are of type DEBUG")
    }


    fun `Exception info displayed correctly lambdas expect NonNullable`() {

        fun subTask1(): Int = runTask("Sub task 1"){
            throw Exception("GenericException")
             10
        }.resultOrException()

        runTask("Task1", TaskConfig(initiator = "TestInstance")) {
            subTask1()
            println("End of subTask1")
        }
    }

    fun subTask2(): Int = runTask("Sub task 2"){
        throw Exception("GenericException")
        10
    }.resultOrException()

    fun `Exception info displayed correctly lambdas expect Unit`() {
        runTask("Task1", TaskConfig(initiator = "TestInstance")) {
            runTask("Sub task 1"){
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

            val stopEvent =  taskHandler.dataProcessor.registerStop()
            val info = taskHandler.info("Info message")
            val warning = taskHandler.warn("Info message")
            val warning2 = taskHandler.warn(ManagedException("Exception Message"), "Additional message")

            assertEquals(SeverityLevel.INFO, info.severity, "Severity mismatch")
            assertEquals(SeverityLevel.WARNING, warning.severity, "Severity mismatch")
            assertEquals(SeverityLevel.EXCEPTION, warning2.severity, "Severity mismatch")
            assertEquals(SeverityLevel.INFO, stopEvent.severity, "Severity mismatch")
        }
    }
}