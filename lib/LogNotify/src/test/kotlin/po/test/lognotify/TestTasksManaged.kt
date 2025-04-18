package po.test.lognotify

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTask
import po.lognotify.extensions.subTask
import po.lognotify.models.LogRecord
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestTasksManaged() : TasksManaged{

    @Test
    fun `tasks nesting creation`() = runTest {

        var hostingObjectParam: String = "hostingObjectParam"
        var parentTaskName: String = ""
        var childTaskName: String = ""
        var executionTime: Float = 0f
        var parentResultTestString: String = ""

        startTask("parent_with_receiver", this.coroutineContext) { parentHelper ->
            parentTaskName = parentHelper.taskName
            val result = parentHelper.taskName
            return@startTask result
        }.onResult { result ->
            parentResultTestString = result
        }.onComplete { parentResult ->
            executionTime = parentResult.executionTime
            assertEquals("parent_with_receiver", parentTaskName, "Parent name")
            assertEquals("child_with_receiver:parent_with_receiver", childTaskName, "Child name")
            assertNotEquals(executionTime, 0f, "Time calculated and greater than 0")
            assertEquals(parentResultTestString, "parent_with_receiver", "Parent result returned(Same as name)")
        }
    }



//    @Test
//    fun `propagated exception handled` () = runTest {
//        var propagatedHandled = false
//        var propagatedExMessage = ""
//
//        startTask("parent", this.coroutineContext) { handler ->
//            handler.setPropagatedExHandler {
//                propagatedHandled = true
//                propagatedExMessage = it.message.toString()
//            }
//            subTask("child") { childHandler ->
//                childHandler.throwPropagatedException(childHandler.taskName)
//            }
//        }.onComplete {
//            assertTrue(propagatedHandled)
//            assertEquals("child:parent", propagatedExMessage, "Propagated exception has message")
//        }
//    }


    @Test
    fun `cancellation exception handled`() = runTest {

        var cancellationHandledOnParent = false
        var cancellationHandled = false
        var exMessage = ""

        startTask("parent", this.coroutineContext) { handler ->
            handler.setCancellationExHandler {
                cancellationHandledOnParent = true
            }
            subTask("child") { childHelper ->
                childHelper.setCancellationExHandler {
                    cancellationHandled = true
                    exMessage = it.message.toString()
                }
                childHelper.throwCancellationException("cancellation")
            }.onComplete { result ->
//                val log = result.getLogRecords(true)
//                assertEquals(2, log.count(), "Log has exactly two records")
//                assertEquals("cancellation", log[1].message, "Message from exception")
            }

        }.onComplete {
            assertTrue(cancellationHandled, "Child cancellation handler triggered")
            assertTrue(!cancellationHandledOnParent, "Parent cancellation handler set but not triggered")
            assertEquals("cancellation", exMessage, "Exception has message")
        }
    }

}