package po.lognotify.test.managedtask

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.test.setup.ForeignClass
import po.managedtask.classes.task.TaskHelper
import po.managedtask.enums.SeverityLevel
import po.managedtask.exceptions.DefaultException
import po.managedtask.extensions.startTask
import po.managedtask.extensions.*
import po.managedtask.extensions.subTask
import po.managedtask.interfaces.TasksManaged
import po.managedtask.models.LogRecord
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTasksManaged() : TasksManaged {

    @Test
    fun `tasks nesting creation`() = runTest {

        var hostingObjectParam : String  = "hostingObjectParam"
        var parentTaskName : String  = ""
        var childTaskName : String = ""
        var executionTime: Float = 0f
        var parentResultTestString: String = ""



        startTask("parent_with_receiver", this.coroutineContext){parentHelper->

            parentTaskName = parentHelper.name
            val result = parentHelper.name
            return@startTask result
//            subTask("child_with_receiver") {childHelper->
//                childTaskName = childHelper.moduleName
//            }
//            result
        }.onResult{result->
           parentResultTestString = result
       }.onComplete{parentResult->
            executionTime = parentResult.executionTime
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
            }.onComplete{result->
                logRecords.addAll(result.getLogRecords(true))

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
            subTask("child") {childHelper->
                throwPropagatedException(childHelper.name)
            }
        }.onComplete{
            assertTrue(propagatedHandled)
            assertEquals("child:parent", propagatedExMessage, "Propagated exception has message")
        }
    }


    @Test
    fun `cancellation exception handled`() = runTest {

        var cancellationHandledOnParent = false
        var cancellationHandled = false
        var exMessage = ""

        startTask("parent", this.coroutineContext){helper->
            setCancellationExHandler{
                cancellationHandledOnParent = true
            }
            subTask("child") {childHelper->
                setCancellationExHandler {
                    cancellationHandled = true
                    exMessage = it.message.toString()
                }

                throwCancellationException("cancellation")
            }.onComplete {result->
                val log =  result.getLogRecords(true)
                assertEquals(2, log.count(), "Log has exactly two records")
                assertEquals("cancellation",log[1].message, "Message from exception")
            }

        }.onComplete{
            assertTrue(cancellationHandled, "Child cancellation handler triggered")
            assertTrue(!cancellationHandledOnParent, "Parent cancellation handler set but not triggered")
            assertEquals("cancellation", exMessage, "Exception has message")
        }
    }

    suspend fun foreignTransition(taskHelper: TaskHelper){
        val foreign = ForeignClass("foreign")
        foreign.transition(taskHelper)
    }

    suspend fun transition(taskHelper: TaskHelper){
//        withTask(key) {
//            info("transited")
//        }
    }

    @Test
    fun `transition to sub task with context retention`()= runTest {

        startTask("parent_task", this.coroutineContext) {
            echo("In parent context")
            transition(this)
            foreignTransition(this)
        }.onComplete{result->
            val log = result.getLogRecords(true)
            assertNotNull(log.firstOrNull{ it.severity == SeverityLevel.INFO && it.message == "transited"})
            assertNotNull(log.firstOrNull{ it.severity == SeverityLevel.INFO && it.message == "foreign"})
        }
    }

    @Test
    fun `task result as an extension`()= runTest {
        var res : String = ""
        startTask("el_tasko", this.coroutineContext) {
            val testStr  : String = "sss"
            return@startTask testStr
        }.onResult {
           assertEquals("sss", it, "onResult as an extension work")
        }
    }

}