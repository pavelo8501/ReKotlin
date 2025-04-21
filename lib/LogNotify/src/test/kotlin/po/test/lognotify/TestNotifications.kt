package po.test.lognotify

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.extensions.startTask
import po.lognotify.extensions.subTask
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestNotifications  : TasksManaged {

    suspend fun subTask(){
        subTask("task_2") {handler->
            handler.apply {
                setFallback(HandlerType.GENERIC){
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

                setFallback(HandlerType.GENERIC){

                }

            }
            termination()
            ManagedException("Default", HandlerType.GENERIC)

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