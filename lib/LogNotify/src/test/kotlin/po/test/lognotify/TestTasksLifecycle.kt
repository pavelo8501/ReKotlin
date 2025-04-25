package po.test.lognotify

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTask
import po.lognotify.extensions.subTask


class TestTasksLifecycle : TasksManaged {

    suspend fun lastExecutedContext() {

    }

    suspend fun childExecutionContext() {

        subTask("child_execution_context_task") {handler->
            handler.echo("child_execution_context_task lambda executed")
        }

        suspend fun structuralNesting() {
            suspend fun structuralNestingLevel3() {
                lastExecutedContext()
            }
            structuralNestingLevel3()
        }
        structuralNesting()
    }

    @Test
    fun `parent registred and child attaches to parent`() = runTest {

        startTask("paren_task", this.coroutineContext) {handler->
            childExecutionContext()
            handler.info("hierarchy_count ${handler.hierarchyRoot().subTasksCount()}")
        }.onComplete {result->
           result.taskName
        }
    }

}