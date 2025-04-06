package po.test.lognotify

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.managedtask.extensions.startTask
import po.managedtask.extensions.subTask
import po.managedtask.interfaces.TasksManaged

class TestTasksLifecycle : TasksManaged {

    suspend fun lastExecutedContext() {

    }


    suspend fun childExecutionContext() {
        subTask("child_execution_context_task") {
            echo("child_execution_context_task lambda executed")

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

        startTask("paren_task", this.coroutineContext) {
            childExecutionContext()
            info("hierarchy_count ${this.taskHierarchyList.count()}")

        }.onComplete {result->
           result.taskName
//            result. .taskHierarchyList.forEach{
//                println("task|${it.taskName}")
//            }


        }
    }

}