package po.test.lognotify.messaging

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.classes.task.result.resultOrNull
import po.lognotify.enums.SeverityLevel
import po.lognotify.extensions.runTask
import po.lognotify.extensions.subTask
import po.misc.exceptions.ManagedException
import kotlin.test.assertEquals

class TestTaskNotifications: TasksManaged {


    @Test
    fun `Exception info displayed correctly lambdas expect NonNullable`() {

        fun subTask1(): Int = subTask("Sub task 1"){
            throw Exception("GenericException")
             10
        }.resultOrException()

        runTask("Task1", TaskConfig(actor = "TestInstance")) {
            subTask1()
            println("End of subTask1")
        }
    }

    fun `Exception info displayed correctly lambdas expect Unit`() {
        runTask("Task1", TaskConfig(actor = "TestInstance")) {
            subTask("Sub task 1"){
                subTask("Sub task 2"){
                    throw Exception("GenericException")
                }
            }.onFail {
                val exception = it
            }
            println("End of Task1 lambda reached")
        }
        println("End of Exception info displayed correctly test reached")
    }

    fun `Task predefined messages work as expected`() {

        runTask("Task1", TaskConfig(actor = "TestInstance")) { handler ->

            val startEvent = handler.dataProcessor.systemEvent(EventType.START)
            val stopEvent =  handler.dataProcessor.systemEvent(EventType.STOP)
            val info = handler.infoV2("Info message")
            val warning = handler.warnV2("Info message")
            val warning2 = handler.warnV2(ManagedException("Exception Message"), "Additional message")

            assertEquals(SeverityLevel.INFO, info.severity, "Severity mismatch")
            assertEquals(SeverityLevel.WARNING, warning.severity, "Severity mismatch")
            assertEquals(SeverityLevel.EXCEPTION, warning2.severity, "Severity mismatch")
            assertEquals(SeverityLevel.INFO, startEvent.severity,"Severity mismatch")
            assertEquals(SeverityLevel.INFO, stopEvent.severity, "Severity mismatch")
        }
    }

    fun `Task notifications`(){
        runTask("Task1"){handler->

            val taskData = TaskData(
                taskKey = handler.task.key,
                config =  handler.task.config,
                timeStamp = handler.task.executionTimeStamp,
                message = "Let the templating era begins",
                severity = SeverityLevel.EXCEPTION
                )

            handler.dataProcessor.provideOutputSource{
                println(it)
            }

            handler.dataProcessor.processRecord(taskData, TaskData.Header)
            handler.dataProcessor.processRecord(taskData, TaskData.Footer)
            handler.dataProcessor.processRecord(taskData, TaskData.Message)


        }

    }
}