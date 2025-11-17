package po.test.misc.data.logging.processor

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.Verbosity
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.Notification
import po.misc.data.logging.processor.logProcessor
import po.misc.io.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestLogProcessor: Component {

    private class SubClass(): Component{
        override val componentID: ComponentID = componentID()
        val subProcessor = logProcessor()
    }

    override val componentID: ComponentID = componentID()

    //Override to jam Component's built in console output
    override fun notify(topic: NotificationTopic, subject: String, text: String): Notification {
       // this one -> notification.output()
       return Notification(this, topic, subject, text)
    }

    private val subClass = SubClass()

    private val subject = "Some Subject"
    private val notificationText = "Some text"

    val initialStep: String = "Initial Step"
    val subClassStep : String = "SubClass Step"


    @Test
    fun `Log processor's console outputs respect host verbosity setting`(){
        componentID.verbosity = Verbosity.Debug
        val processor = logProcessor()

        val debug = notify(NotificationTopic.Debug, "Some subject", notificationText)
        var capturedDebug = captureOutput {
            processor.logData(debug.toLogMessage())
        }
        assertTrue {
            capturedDebug.output.contains(notificationText)
        }
        componentID.verbosity = Verbosity.Info
        capturedDebug = captureOutput {
            processor.logData(debug.toLogMessage())
        }
        assertFalse {
            capturedDebug.output.contains(notificationText)
        }

        val info = notify(NotificationTopic.Info, "Info subject", notificationText)
        var capturedInfo = captureOutput {
            processor.logData(info.toLogMessage())
        }
        assertTrue {
            capturedInfo.output.contains(notificationText)
        }
        componentID.verbosity = Verbosity.Warnings
        capturedInfo = captureOutput {
            processor.logData(info.toLogMessage())
        }
        assertFalse {
            capturedInfo.output.contains(notificationText)
        }

        val warning = notify(NotificationTopic.Warning, "Warning subject", notificationText)
        var capturedWarning = captureOutput {
            processor.logData(warning.toLogMessage())
        }
        assertTrue {
            capturedWarning.output.contains(notificationText)
        }
        //Backwards check if warnings are not effected by Verbosity
        componentID.verbosity = Verbosity.Info
        capturedWarning = captureOutput {
            processor.logData(warning.toLogMessage())
        }
        assertTrue {
            capturedWarning.output.contains(notificationText)
        }

        componentID.verbosity = Verbosity.Debug
        capturedWarning = captureOutput {
            processor.logData(warning.toLogMessage())
        }
        assertTrue {
            capturedWarning.output.contains(notificationText)
        }
    }

    @Test
    fun `Log processor stores data disregarding verbosity`(){

        val processor = logProcessor()

        componentID.verbosity = Verbosity.Warnings
        val debug = notify(NotificationTopic.Debug, subject, notificationText)
        processor.logData(debug.toLogMessage())
        assertNotNull(processor.logRecords.firstOrNull { it.topic == NotificationTopic.Debug })

        val info = notify(NotificationTopic.Info, subject, notificationText)
        processor.logData(info.toLogMessage())
        assertEquals(2, processor.logRecords.size)
        assertNotNull(processor.logRecords.firstOrNull { it.topic == NotificationTopic.Info })

        val warning = notify(NotificationTopic.Warning, subject, notificationText)
        processor.logData(warning.toLogMessage())
        assertEquals(3, processor.logRecords.size)
        assertNotNull(processor.logRecords.firstOrNull { it.topic == NotificationTopic.Warning })
    }

    @Test
    fun `Log processor's collectData lambda work as expected`(){
        val processor = logProcessor()
        var intercepted: Loggable? = null
        processor.collectData(keepData = true){ intercepted = it }
        var info = notify(NotificationTopic.Info, subject, notificationText)
        processor.logData(info.toLogMessage())
        assertNotNull(intercepted)
        assertNotNull(processor.logRecords.firstOrNull { it.topic === NotificationTopic.Info })

    }

}