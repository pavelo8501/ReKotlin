package po.test.lognotify

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.managedtask.extensions.startTask
import po.managedtask.extensions.subTask
import po.managedtask.interfaces.TasksManaged
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestNotifications  :TasksManaged {

    suspend fun subTask(){
        subTask("task_2") {
            setGenericExHandler {
                echo("Swallowed")
            }
          //  termination()
            throw Exception("General exception")
        }
    }

    suspend fun termination(){
        subTask("task_3") {
            throw Exception("Unhandled exception")
        }
    }

    @Test
    fun `console error printout`() = runTest{
        var unprocessedThrowable : Throwable?  = null
        var swallowedThrowable : Throwable?  = null
        var onCompleteTriggered : Boolean = false

        startTask("task_1", this.coroutineContext) {
            setGenericExHandler {
                swallowedThrowable = it
            }
            termination()
            throwDefaultException("Default")
        }.onFail {
            unprocessedThrowable = it
        }.onComplete {
            onCompleteTriggered = true
        }
        assertNotNull(swallowedThrowable)
        assertNull(unprocessedThrowable)
        assertTrue(onCompleteTriggered)
    }

}