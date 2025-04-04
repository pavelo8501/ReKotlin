package po.lognotify.test.managedtask

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.managedtask.exceptions.DefaultException
import po.managedtask.extensions.startTask
import po.managedtask.interfaces.TasksManaged
import po.managedtask.models.LogRecord
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestTasksManaged() : TasksManaged {

    override val personalName: String  = "Test_Container"

    @Test
    fun `tasks nesting creation`() = runTest {

        var hostingObjectParam : String  = "hostingObjectParam"
        var parentTaskName : String  = ""
        var childTaskName : String = ""
        var executionTime: Float = 0f
        var parentResultTestString: String = ""


        startTask("parent_with_receiver", this.coroutineContext){parentHelper->
            parentTaskName = parentHelper.moduleName
            val result = parentHelper.moduleName
            startTask("child_with_receiver") {childHelper->
                childTaskName = childHelper.moduleName
            }
            result
        }.onSuccess{parentResult->
           parentResultTestString = parentResult.extractResult()?:""
           executionTime = parentResult.executionTime
       }.onComplete{
            assertEquals("parent_with_receiver", parentTaskName, "Parent name")
            assertEquals("child_with_receiver:parent_with_receiver", childTaskName, "Child name")
            assertNotEquals(executionTime, 0f, "Time calculated and greater than 0")
            assertEquals(parentResultTestString, "parent_with_receiver", "Parent result returned(Same as name)")
       }
    }

    @Test
    fun `tasks logging`()= runTest {
            val logRecords = mutableListOf<LogRecord>()
            startTask("parent", this.coroutineContext){
                info("InfoMessage")
                warn("Warning Message")
                error(DefaultException("Exceptions Message"))
            }.onComplete{
                logRecords.addAll(it)

                assertEquals("InfoMessage",logRecords[0].message, "First record message match")
                assertEquals("Exceptions Message",logRecords[2].message, "Message from exception")
                assertEquals(3,logRecords.count(), "Log has exactly 3 items")
            }
    }


    @Test
    fun `propagated exception handled` () = runTest {
        var propagatedHandled = false
        var propagatedExMessage =""

        startTask("parent", this.coroutineContext){helper->
            setPropagatedExHandler {
                propagatedHandled = true
                propagatedExMessage = it.message.toString()
            }
            startTask("child"){childHelper->
                throwPropagatedException(childHelper.moduleName)
            }
        }.onComplete{
            assertTrue(propagatedHandled)
            assertEquals("child:parent", propagatedExMessage, "Propagated exception has message")
        }
    }

    @Test
    fun `cancellation work exception work`() = runTest {

        var cancellationHandledOnParent = false
        var cancellationHandled = false
        var exMessage = ""

        startTask("parent", this.coroutineContext){helper->
            setCancellationExHandler{
                cancellationHandledOnParent = true
            }
            startTask("child"){childHelper->
                setCancellationExHandler {
                    cancellationHandled = true
                    exMessage = it.message.toString()
                }
                throwCancellationException("cancellation")
            }.onComplete {
                assertEquals(2, it.count(), "Log has exactly two records")
                assertEquals("cancellation",it[1].message, "Message from exception")
            }
        }.onComplete{
            assertTrue(cancellationHandled, "Child cancellation handler triggered")
            assertTrue(!cancellationHandledOnParent, "Parent cancellation handler set but not triggered")
            assertEquals("cancellation", exMessage, "Exception has message")
        }.onSuccess{result->
            result.printLog(true)
        }
    }
}