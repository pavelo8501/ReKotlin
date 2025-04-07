package po.test.lognotify

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTask
import po.lognotify.extensions.subTask
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestNotifications  : TasksManaged {

    suspend fun subTask(){
        subTask("task_2") {handler->
            handler.apply {
                setGenericExHandler {
                    echo("Swallowed")
                }
            }
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

        startTask("task_1", this.coroutineContext) {handler->
            handler.apply {
                setGenericExHandler {
                    swallowedThrowable = it
                }
            }
            termination()
            handler.throwDefaultException("Default")

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