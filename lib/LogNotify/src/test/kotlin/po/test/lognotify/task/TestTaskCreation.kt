package po.test.lognotify.task

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.dispatcher.createHierarchyRoot
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.Task
import po.lognotify.tasks.createTask
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestTaskCreation: TasksManaged {

    override val identity: CTXIdentity<TestTaskCreation> = asIdentity()
    internal val config = TaskConfig()

    @Test
    fun `Root task creation helpers work as expected`() {
        val rootTask = createHierarchyRoot<TestTaskCreation, Unit>("Task_1", this, config)
        val rootTaskByDispatcher = logHandler.dispatcher.createHierarchyRoot<TestTaskCreation, Unit>("Task_1", this, config)
        assertIs<RootTask<*, *>>(rootTask)
        assertIs<RootTask<*, *>>(rootTaskByDispatcher)

        assertEquals(this, rootTask.receiver)
        assertEquals(this, rootTaskByDispatcher.receiver)
    }

    @Test
    fun `Sub task creation logic work as expected`() {
        val rootTask = createHierarchyRoot<TestTaskCreation, Unit>("Task_1", this, config)
        val task = rootTask.createTask<TestTaskCreation, Unit>("Task_1_1", this, config)
        assertIs<Task<TestTaskCreation, Unit>>(task)
        assertEquals(1, task.key.nestingLevel)
        assertEquals(rootTask, task.rootTask)
        assertEquals(rootTask, task.parentTask)
        assertEquals(2, rootTask.registry.totalCount)

        val task1 = task.createTask<TestTaskCreation, Unit>("Task_1_1_1", this, config)
        assertEquals(2, task1.key.nestingLevel)
        assertEquals(rootTask, task1.rootTask)
        assertIs<Task<TestTaskCreation, Unit>>(task1)
        assertEquals(task, task1.parentTask)
        assertEquals(3, rootTask.registry.totalCount)

        assertNotEquals(task1.key, task.key)
        assertNotEquals(task.key, rootTask.key)
    }

    @Test
    fun `Tasks components properly created`() {
        val rootTask = createHierarchyRoot<TestTaskCreation, Unit>("Task_1", this, config)
        val task = rootTask.createTask<TestTaskCreation, Unit>("Task_1_1", this, config)
        val task1 = task.createTask<TestTaskCreation, Unit>("Task_1_1_1", this, config)

        assertEquals(task1.coroutineContext, task.coroutineContext)
        assertEquals(task.coroutineContext, rootTask.coroutineContext)

        assertNotNull(rootTask.dataProcessor.flowEmitter)
        assertNull(task.dataProcessor.flowEmitter)
        assertNull(task1.dataProcessor.flowEmitter)

    }

}