package po.test.misc.data.logging.processor

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.tracable.Notification
import po.misc.context.tracable.NotificationTopic
import po.misc.data.logging.Verbosity
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.logProcessor
import po.misc.io.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestLogProcessor: Component {

    override val componentID: ComponentID = componentID()

    //Override to jam Component's built in console output
    override fun notify(topic: NotificationTopic, subject: String, text: String): Notification {
       // this one -> notification.output()
       return Notification(this, topic, subject, text)
    }

    private val subject = "Some Subject"
    private val notificationText = "Some text"

    @Test
    fun `Log processor's console outputs respect host verbosity setting`(){
        componentID.verbosity = Verbosity.Debug
        val processor = LogProcessor<TestLogProcessor, Notification>(this)

        val debug = notify(NotificationTopic.Debug, "Some subject", notificationText)
        var capturedDebug = captureOutput {
            processor.logData(debug)
        }
        assertTrue {
            capturedDebug.output.contains(notificationText)
        }
        componentID.verbosity = Verbosity.Info
        capturedDebug = captureOutput {
            processor.logData(debug)
        }
        assertFalse {
            capturedDebug.output.contains(notificationText)
        }

        val info = notify(NotificationTopic.Info, "Info subject", notificationText)
        var capturedInfo = captureOutput {
            processor.logData(info)
        }
        assertTrue {
            capturedInfo.output.contains(notificationText)
        }
        componentID.verbosity = Verbosity.Warnings
        capturedInfo = captureOutput {
            processor.logData(info)
        }
        assertFalse {
            capturedInfo.output.contains(notificationText)
        }

        val warning = notify(NotificationTopic.Warning, "Warning subject", notificationText)
        var capturedWarning = captureOutput {
            processor.logData(warning)
        }
        assertTrue {
            capturedWarning.output.contains(notificationText)
        }
        //Backwards check if warnings are not effected by Verbosity
        componentID.verbosity = Verbosity.Info
        capturedWarning = captureOutput {
            processor.logData(warning)
        }
        assertTrue {
            capturedWarning.output.contains(notificationText)
        }

        componentID.verbosity = Verbosity.Debug
        capturedWarning = captureOutput {
            processor.logData(warning)
        }
        assertTrue {
            capturedWarning.output.contains(notificationText)
        }
    }

    @Test
    fun `Log processor stores data disregarding verbosity`(){

        val processor = logProcessor(Notification.Companion)

        componentID.verbosity = Verbosity.Warnings
        val debug = notify(NotificationTopic.Debug, subject, notificationText)
        processor.logData(debug)
        assertNotNull(processor.records.firstOrNull { it.topic == NotificationTopic.Debug })

        val info = notify(NotificationTopic.Info, subject, notificationText)
        processor.logData(info)
        assertEquals(2, processor.records.size)
        assertNotNull(processor.records.firstOrNull { it.topic == NotificationTopic.Info })

        val warning = notify(NotificationTopic.Warning, subject, notificationText)
        processor.logData(warning)
        assertEquals(3, processor.records.size)
        assertNotNull(processor.records.firstOrNull { it.topic == NotificationTopic.Warning })
    }

    @Test
    fun `Log processor's collectData lambda work as expected`(){
        val processor = logProcessor(Notification.Companion)
        var intercepted: Notification? = null
        processor.collectData(keepData = true){ intercepted = it }
        var info = notify(NotificationTopic.Info, subject, notificationText)
        processor.logData(info)
        assertNotNull(intercepted)
        assertNotNull(processor.records.firstOrNull { it === info })

        info = notify(NotificationTopic.Info, subject, notificationText)
        processor.clear()
        intercepted = null
        processor.collectData(keepData = false){ intercepted = it }
        processor.logData(info)
        assertNotNull(intercepted)
        assertNull(processor.records.firstOrNull { it === info })
    }

}