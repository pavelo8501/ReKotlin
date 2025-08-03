package po.test.lognotify.task

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.interfaces.FakeTasksManaged
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestTaskFlow : FakeTasksManaged {
    override val contextName: String = "TestTaskFlow"

    companion object {
        @JvmStatic
        var retryCount: Int = 0
    }

    fun subTask(
        th: Throwable?,
        retries: Int,
    ): Int =
        runTask("SubTask", TaskConfig(attempts = retries)) {
            retryCount++
            if (th != null) {
                throw th
            }
            10
        }.resultOrException()

    @Test
    fun `Task delay and retry logic work as expected`() {
        retryCount = 0
        val expectedRetries = 2
        runTask("Entry task") {
            assertThrows<ManagedException> {
                subTask(Exception("General"), expectedRetries)
            }
        }
        assertEquals(expectedRetries, retryCount)
    }

    @Test
    fun `Default root task is created to avoid crash and warning issued`() {
        assertDoesNotThrow {
            notify("Some message", SeverityLevel.INFO)
        }
    }

    @Test
    fun `Consequent tasks inherit task configuration if not explicitly overriden`() {
        var taskConfig: TaskConfig? = null
        val entryTaskConfig = TaskConfig(exceptionHandler = HandlerType.CancelAll)
        runTask<TestTaskFlow, Unit>("Entry task", entryTaskConfig) {
            runTask<TestTaskFlow, Unit>("Nested Root task") {
                runTask("Sub task") {
                    taskConfig = taskHandler.taskConfig
                }
            }
        }
        assertEquals(entryTaskConfig, taskConfig)
    }
}
