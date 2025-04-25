package po.test.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.lognotify.TasksManaged
import po.lognotify.classes.task.UpdateType
import po.lognotify.extensions.newTask
import po.lognotify.extensions.subTask

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestLoggableProcess : TasksManaged {

    @BeforeAll
    fun printTaskUpdates(){
        TasksManaged.onTaskCreated(UpdateType.OnStart){
            println(it)
        }
    }

    @Test
    fun `test tasks lifecycle call #1`() = runTest{

        for (i in 1..5) {
            launch(CoroutineName("Task $i")) {
                newTask("Task $i") {topHandler->
                    for (a in 1..2) {
                        subTask("Sub Task $a"){subHandler->
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `test tasks lifecycle call #2`() = runTest{
        for (i in 1..5) {
            launch(CoroutineName("Task $i")) {
                newTask("Task $i") {topHandler->
                    println("Task $i")
                    for (a in 1..2) {
                        subTask("Sub Task $a") {
                        }
                    }
                }
            }
        }
    }
}