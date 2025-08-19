package po.test.lognotify.task

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.common.configuration.TaskType
import po.lognotify.launchers.runTask
import po.lognotify.tasks.RootTask
import po.test.lognotify.setup.FakeTasksManaged
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import kotlin.test.assertEquals
import kotlin.test.assertIs

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestTaskFlow : FakeTasksManaged {


    companion object {
        @JvmStatic
        var retryCount: Int = 0
    }

    @AfterEach
    fun reset(){
        retryCount = 0
    }

    fun subTask(th: Throwable?, retries: Int): Int = runTask("SubTask", TaskConfig(attempts = retries)) {
        retryCount++
        if (th != null) { throw th }
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
    fun `Consequent tasks inherit task configuration if not explicitly overwritten`() {
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

    @Test
    fun `Task crated as root if stated in config`() {
        val taskConfig = TaskConfig(taskType = TaskType.AsRootTask)
        val rootTask =  mockRootTask()
        val newRoot = rootTask.mockChildTask("child_should_be_root", taskConfig)
        assertIs<RootTask<TestTaskFlow, Unit>>(newRoot)

    }


}
