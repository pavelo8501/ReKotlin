package po.test.lognotify

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTask
import po.lognotify.extensions.subTask
import po.lognotify.models.LogRecord
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
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
            parentTaskName = parentHelper.task.taskData.taskName
            val result = parentHelper.task.taskData.taskName
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

            handler.handleFailure(HandlerType.CANCEL_ALL){
                cancellationHandledOnParent = true
            }


            subTask("child") { childHelper ->

                handler.handleFailure(HandlerType.CANCEL_ALL){
                    cancellationHandled = true

                }
                ManagedException("cancellation")

            }.onComplete { result ->

            }

        }.onComplete {
            assertTrue(cancellationHandled, "Child cancellation handler triggered")
            assertTrue(!cancellationHandledOnParent, "Parent cancellation handler set but not triggered")
            assertEquals("cancellation", exMessage, "Exception has message")
        }
    }

}