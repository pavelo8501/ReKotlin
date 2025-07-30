package po.test.lognotify.launchers

import org.junit.jupiter.api.Test
import po.lognotify.action.ActionSpan
import po.lognotify.common.containers.ActionContainer
import po.lognotify.common.containers.TaskContainer
import po.lognotify.interfaces.FakeTasksManaged
import po.misc.containers.withReceiverAndResult
import po.misc.data.processors.SeverityLevel
import po.test.lognotify.setup.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestLaunchers: FakeTasksManaged {


    val testLauncherProperty: String = "TestLauncherProperty"

    @Test
    fun `All launch functions preserve access to original context`(){

       val inputMessage = "Some message"
       val taskContainer =  captureOutput<TaskContainer<TestLaunchers, Unit>> {
             val rootTask = dispatcher.createHierarchyRoot<TestLaunchers, Unit>("Root Task", this)
             TaskContainer(rootTask)
        }.result
        var output: String = ""
         taskContainer.withReceiverAndResult {
          output =  captureOutput {
                notify(inputMessage, SeverityLevel.INFO)
            }
            assertIs<TestLaunchers>(this, "This context is lost")
            assertEquals("TestLauncherProperty", testLauncherProperty, "testLauncherProperty is not accessible")
        }
        assertTrue(output.contains(inputMessage), "Outer containers method notify is not accessible")

        val inputInsideAction = "Some Message Inside Action"
        val actionContainer =  captureOutput<ActionContainer<TestLaunchers, Unit>> {
            val rootTask = dispatcher.createHierarchyRoot<TestLaunchers, Unit>("Root Task", this)
            val span = ActionSpan<TestLaunchers, Unit>("Span", this,  rootTask)
            ActionContainer(span)
        }.result

        actionContainer.withReceiverAndResult {
            output =  captureOutput {
                notify(inputInsideAction, SeverityLevel.INFO)
            }

            assertIs<TestLaunchers>(this, "This context is lost")
            assertEquals("TestLauncherProperty", testLauncherProperty, "testLauncherProperty is not accessible")
        }
        assertTrue(output.contains(inputInsideAction), "Outer containers method notify is not accessible")

    }

}