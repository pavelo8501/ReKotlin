package po.test.lognotify.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.launchers.runTask
import po.lognotify.launchers.runTaskAsync
import po.lognotify.launchers.runTaskBlocking
import po.test.lognotify.setup.FakeTasksManaged
import po.lognotify.models.LoggerStats
import po.lognotify.models.TaskDispatcher
import po.lognotify.tasks.TaskHandler
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestTaskBase : FakeTasksManaged {
    override val contextName: String = "TestTaskBase"

    data class TaskLaunchParam(
        val taskName: String,
        val childTaskCount: Int,
        val delay: Long,
    )

    class ReceiverClass : FakeTasksManaged {
        override val contextName: String = "ReceiverClass"

        fun function1(
            input: Int,
            childCount: Int,
            callback: ((TaskHandler<Int>) -> Unit)? = null,
        ): Int =
            runTaskBlocking("task_function1") { handler ->
                callback?.invoke(handler)
                for (i in 1..childCount) {
                    childTask(i.toString())
                }
                input
            }.resultOrException()

        suspend fun runAsynchronously(launchParam: TaskLaunchParam): Int {
            var totalTasks: Int = 0
            runTaskAsync(launchParam.taskName) {
                for (a in 1..launchParam.childTaskCount) {
                    delay(launchParam.delay)
                    childTask("${launchParam.taskName}_child_$a")
                    totalTasks++
                }
            }
            return totalTasks
        }

        suspend fun childTask(taskName: String) =
            runTaskAsync(taskName) {
            }
    }

    @Test
    fun `Task hierarchy creation in asynchronous mode`() {
        val receiverClass = ReceiverClass()
        val taskStats = mutableListOf<LoggerStats>()

        logHandler.dispatcher.onTaskUpdate(TaskDispatcher.UpdateType.OnTaskCreated) {  }

        receiverClass.function1(input = 1, childCount = 10)

        assertAll(
            "Creation order is preserved",
            { assertEquals(11, taskStats.size) },
            { assertEquals("task_function1", taskStats[0].activeTask.key.taskName, "Root task name mismatch") },
            { assertEquals("1", taskStats[1].activeTask.key.taskName, "Task name mismatch") },
            { assertEquals("10", taskStats[10].activeTask.key.taskName, "Task name mismatch") },
        )

        taskStats.clear()
        logHandler.dispatcher.onTaskUpdate(TaskDispatcher.UpdateType.OnTaskCreated) {

        }

        val topTaskName = "TopTask"
        val rootTask1Name = "RootTask1"
        val rootTask2Name = "RootTask2"

        runTaskBlocking(topTaskName, TaskConfig().launchOptions.setDispatcher(Dispatchers.IO) ) {
            runTaskAsync(rootTask1Name) {
                for (i in 1..20) {
                    runTask("${rootTask1Name}_child_$i") {
                    }
                }
            }
            receiverClass.runAsynchronously(TaskLaunchParam(taskName = rootTask2Name, childTaskCount = 1, delay = 0))
        }

        val roots = taskStats.map { it.activeTask }.filter { it.key.nestingLevel == 0 }
        assertEquals(3, roots.size, "Expected 3 root tasks. 2 in parallel")

        val topTask = assertNotNull(roots.firstOrNull { it.key.taskName == topTaskName }, "$topTaskName not listed")

        val rootTask1 = assertNotNull(roots.firstOrNull { it.key.taskName == rootTask1Name }, "$rootTask1Name not listed")
        val rootTask2 = assertNotNull(roots.firstOrNull { it.key.taskName == rootTask2Name }, "$rootTask2Name not listed")
        val topTaskCoroutine = topTask.coroutineInfo
        val rootTask1Coroutine = rootTask1.coroutineInfo
        val rootTask2Coroutine = rootTask2.coroutineInfo

        assertAll(
            "Root tasks executed in parallel",
            { assertNotEquals(rootTask1.key.taskId, rootTask2.key.taskId, "This is same task") },
            { assertNotEquals(rootTask1Coroutine.coroutineName, rootTask2Coroutine.coroutineName, "Coroutine name is the same") },
            { assertNotEquals(rootTask1Coroutine.hashCode, rootTask2Coroutine.hashCode, "This is the same coroutine") },
            { assertEquals("DefaultScheduler", topTaskCoroutine.dispatcherName, "Wrong dispatcher used.") },
        )
    }
}
