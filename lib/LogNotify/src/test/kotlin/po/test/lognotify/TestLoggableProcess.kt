package po.test.lognotify

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.process.ProcessableContext
import po.lognotify.classes.task.UpdateType
import po.lognotify.extensions.launchProcess
import po.lognotify.extensions.newTask
import po.lognotify.extensions.subTask
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class AuthorizedSessionMock internal constructor(
    var name : String =  "AnonymousSession",
): ProcessableContext<AuthorizedSessionMock>, CoroutineContext.Element {

    val sessionID: String = UUID.randomUUID().toString()
    var receivedNotification : Boolean  = false
    var receivedOnProcessStart : Boolean  = false
    var receivedOnProcessEnd : Boolean  = false


    override fun onNotification(notification: Notification){
        receivedNotification = true
        println("ReceivedNotification true")
    }

    override fun onProcessStart(session: LoggProcess<*>) {
        receivedOnProcessStart = true
    }
    override fun onProcessEnd(session: LoggProcess<*>) {
        receivedOnProcessEnd = true
    }

    override val key: AuthorizedSessionMocKey
        get() {
            return AuthorizedSessionMock
        }
    companion object AuthorizedSessionMocKey : CoroutineContext.Key<AuthorizedSessionMock>
}



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestLoggableProcess : TasksManaged {

    companion object{
        @JvmStatic
        var notificationFromTaskDispatcher : Boolean = false
    }

    @BeforeAll
    fun printTaskUpdates(){
        TasksManaged.onTaskCreated(UpdateType.OnStart){
            notificationFromTaskDispatcher = true
        }
    }

    suspend fun doSomething(): Boolean = newTask("Some Task"){
      coroutineContext[LoggProcess] != null
    }.resultOrException()

    @Test
    fun `test tasks lifecycle call #1`() = runTest {

        for (i in 1..5) {
            launch(CoroutineName("Task $i")) {
                newTask("Task $i") { topHandler ->
                    for (a in 1..2) {
                        subTask("Sub Task $a") { subHandler ->
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `test tasks lifecycle call #2`() = runTest{
        for (i in 1..5) {
            launch(CoroutineName("Task $i")) {
                newTask("Task $i") {topHandler->
                    println("Task $i")
                    for (a in 1..2) {
                        subTask("Sub Task $a") {
                        }
                    }
                }
            }
        }
    }


    @Test
    fun `process receive updates`() =runTest {

        val mockedSession = AuthorizedSessionMock("MockedSession")
        var sessionInContext : AuthorizedSessionMock? = null
        var loggProcess : LoggProcess<*>? = null
        var  loggProcessFound = false

       val result = mockedSession.launchProcess {
            sessionInContext = coroutineContext[AuthorizedSessionMock]
            loggProcess = coroutineContext[LoggProcess]
            newTask("Some Task") {

            }
            loggProcessFound = doSomething()
            val result = 10
            result
        }

        assertEquals(10, result, "Result not returned")
        assertNotNull(sessionInContext)
        assertNotNull(loggProcess)
        assertTrue(loggProcessFound)
        assertTrue(notificationFromTaskDispatcher)
        assertTrue(mockedSession.receivedOnProcessStart, "receivedOnProcessStart")
        assertTrue(mockedSession.receivedOnProcessEnd, "receivedOnProcessEnd")
    }

}