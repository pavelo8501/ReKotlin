package po.test.lognotify.notifictations

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.common.containers.TaskContainer
import po.lognotify.common.result.createFaultyResult
import po.lognotify.exceptions.handleException
import po.lognotify.launchers.runAction
import po.lognotify.launchers.runTask
import po.test.lognotify.setup.FakeTasksManaged
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskHandler
import po.misc.exceptions.ManagedException
import po.misc.types.getOrManaged
import po.test.lognotify.setup.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestExceptionNotification: FakeTasksManaged {

    val nullableString: String? = null

    @Test
    fun `Exception notification contains precise exception place`() {
        var exception: ManagedException? = null
        lateinit var rootTask: RootTask<TestExceptionNotification, Unit>
        captureOutput {
            rootTask = mockedDispatcher.createHierarchyRoot("Root Task", this)
        }
        val container = TaskContainer(rootTask)
        exception = assertThrows<ManagedException> {
            nullableString.getOrManaged(String::class, this@TestExceptionNotification)
        }

        val thrownException = assertNotNull(exception)
        createFaultyResult(thrownException, rootTask)
        handleException(thrownException, container, null)
        val firstMessage = assertNotNull(rootTask.dataProcessor.records.firstOrNull())

        val exceptionRecord = assertNotNull(firstMessage.errors.records.firstOrNull())
        assertEquals(rootTask.header, exceptionRecord.firstRegisteredInTask)

        val methodThrowing = assertNotNull(exceptionRecord.methodThrowing)
        assertEquals("getOrManaged", methodThrowing.methodName)

        val throwingCallSite = assertNotNull(exceptionRecord.throwingCallSite)

        assertEquals(this::class.qualifiedName.toString(), throwingCallSite.fileName)
        assertTrue(throwingCallSite.fileName.contains("TestExceptionNotification"))
    }

    @Test
    fun `Exception notification contains information regarding failing ActionSpan`() {
        lateinit var handler : TaskHandler<*>
        captureOutput {
            runTask("Root task") {
                handler = taskHandler
                runAction("Firs Action") {
                    assertThrows<ManagedException> {
                        runAction("Second Action"){
                            nullableString.getOrManaged(String::class, this@TestExceptionNotification)
                        }
                    }
                }
            }
        }
        val taskDataProcessor: LoggerDataProcessor = handler.dataProcessor
        val firstMessage = assertNotNull(taskDataProcessor.records.firstOrNull())
        val exceptionRecord = assertNotNull(firstMessage.errors.records.firstOrNull())
        assertEquals(handler.task.header, exceptionRecord.firstRegisteredInTask)
        val methodThrowing = assertNotNull(exceptionRecord.methodThrowing)
        assertTrue(methodThrowing.methodName.contains("getOrManaged"), "Actual: ${methodThrowing.methodName}")

        val throwingCallSite = assertNotNull(exceptionRecord.throwingCallSite)

        assertTrue(throwingCallSite.fileName.contains("TestExceptionNotification"))
        assertNotNull(exceptionRecord.actionSpans?.lastOrNull { it.actionStatus == ExecutionStatus.Failing })

        exceptionRecord.echo()
    }
}