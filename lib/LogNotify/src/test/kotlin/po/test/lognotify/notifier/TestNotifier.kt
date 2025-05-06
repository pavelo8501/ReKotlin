package po.test.lognotify.notifier

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.extensions.newTask
import po.lognotify.extensions.subTask
import po.lognotify.logNotify
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestNotifier : TasksManaged {

    companion object: TasksManaged{
        @JvmStatic
        val loggerHandler: LogNotifyHandler  = logNotify()
    }

    @BeforeAll
    fun setup(){

    }

    @Test
    fun `configurable logger noise level`()= runTest{

        loggerHandler.notifierConfig {
            muteConsoleNoEvents = true
        }

        var muteConsoleNoEvents: Boolean = false

        newTask("Task1"){
            subTask("Task2"){handler->
                muteConsoleNoEvents = handler.notifier.config.muteConsoleNoEvents
                handler.info("Some message")
            }
        }
        assertTrue(muteConsoleNoEvents, "Configuration did not propagated")
    }

}