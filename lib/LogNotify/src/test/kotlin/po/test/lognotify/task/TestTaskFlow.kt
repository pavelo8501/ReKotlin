package po.test.lognotify.task

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.lognotify.TasksManaged
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.extensions.subTask
import po.misc.exceptions.HandlerType
import kotlin.test.assertEquals

class TestTaskFlow: TasksManaged {

    override val contextName: String = "TestTaskFlow"

    @Test
    fun `Task message issued`(){
        runTask("Entry task"){

        }

    }


    fun `Default root task is created to avoid crash and warning issued`(){

        assertDoesNotThrow {
            logHandler.info("Some message")
        }
    }


    fun `Consequent tasks inherit task configuration if not explicitly overriden`(){
        var taskConfig: TaskConfig? = null
        val entryTaskConfig = TaskConfig(exceptionHandler = HandlerType.CancelAll)
        runTask<TestTaskFlow, Unit>("Entry task", entryTaskConfig){
            runTask<TestTaskFlow, Unit>("Nested Root task"){
                subTask("Sub task"){handler->
                    taskConfig = handler.taskConfig
                }
            }
        }
        assertEquals(entryTaskConfig, taskConfig)
    }
}