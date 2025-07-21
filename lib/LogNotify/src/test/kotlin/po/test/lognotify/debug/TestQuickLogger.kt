package po.test.lognotify.debug

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.extensions.runTaskBlocking
import po.lognotify.interfaces.FakeTasksManaged

class TestQuickLogger() : FakeTasksManaged {


    override val contextName: String = "TestQuickLogger"

    @Test
    fun `DSL type logging get current task in context`(){

        runTaskBlocking("Task1"){


        }

    }

}