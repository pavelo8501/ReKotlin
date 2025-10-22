package po.test.misc.data.logging.processor

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.LogProvider
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.Notification
import po.misc.data.logging.processor.logProcessor
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.grouping.PrintableProperty
import po.misc.data.printable.grouping.createProperty
import po.misc.types.token.TypeToken
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class CustomNotification(
    override val context: TraceableContext,
    override val topic: NotificationTopic,
    override val subject: String,
    override val text: String,
): PrintableBase<CustomNotification>(this), Loggable{

    override val self: CustomNotification = this
    override val created: Instant = Instant.now()

    val subNotifications: PrintableProperty<Notification> = createProperty("subNotifications")

    companion object: PrintableCompanion<CustomNotification>(TypeToken.create())
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestLogProcessorLogProvider: LogProvider<CustomNotification> {

    private class SubComponent(name: String):Component {
        override val componentID: ComponentID = ComponentID(name, this)
        val processor = logProcessor(Notification)

        override fun notify(topic: NotificationTopic, subject: String, text: String): Notification {
           val notification = Notification(this,  topic, subject, text)
           processor.logData(notification)
           return notification
        }

    }

    override val componentID: ComponentID = componentID("Parent Component")

    private val subject = "Some Subject"
    private val notificationText = "Some text"

    private val processor = logProcessor()

    private val child1 = SubComponent("child1")

    override fun notify(topic: NotificationTopic, subject: String, text: String):CustomNotification{
        val notification = CustomNotification(this, topic, subject, text)
        processor.logData(notification)
        return notification
    }

    @BeforeAll
    fun checkSetup(){
        assertNotNull(processor)
        assertNotNull(componentID)
    }

    @Test
    fun `LogProcessor data collection work as expected`(){

        child1.processor.collectData(keepData = false){
            processor.activeRecord?.subNotifications?.add(it)
        }

        notify(NotificationTopic.Info, subject, notificationText)

        child1.notify(NotificationTopic.Warning, "Some warning", "With text")

        assertEquals(1, processor.records.size)
        val firstRec = assertNotNull(processor.records.firstOrNull())
        assertEquals(1, firstRec.subNotifications.size)
        val firstSubNotification = assertNotNull(firstRec.subNotifications.firstOrNull())
        assertEquals(NotificationTopic.Warning, firstSubNotification.topic)
        assertEquals("Some warning", firstSubNotification.subject)
        assertEquals("With text", firstSubNotification.text)
    }


}