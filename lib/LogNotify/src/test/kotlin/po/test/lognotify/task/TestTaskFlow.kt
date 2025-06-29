package po.test.lognotify.task

import org.junit.jupiter.api.Test
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.extensions.subTask
import po.misc.exceptions.HandlerType
import po.misc.interfaces.IdentifiableContext
import kotlin.test.assertEquals

class TestTaskFlow: IdentifiableContext {

    override val contextName: String = "TestTaskFlow"

    @Test
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