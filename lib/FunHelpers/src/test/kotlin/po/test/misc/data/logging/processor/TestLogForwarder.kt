package po.test.misc.data.logging.processor

import org.junit.jupiter.api.Test
import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogForwarder
import po.misc.data.logging.processor.LogHandler
import po.misc.data.logging.processor.createLogProcessor
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestLogForwarder : Component {

    interface MockLoggable : StructuredLoggable

    class MockRecord(loggable: Loggable): MockLoggable {

        override val context: TraceableContext = loggable.context
        override  val topic: NotificationTopic = loggable.topic
        override val subject: String = loggable.subject
        override val text: String = loggable.text
        override val created: Instant = Instant.now()
        override val formattedString: String = loggable.formattedString

        val records = mutableListOf<StructuredLoggable>()

        val mockRecords  = mutableListOf<Loggable>()

        override val tracker: Enum<*>? = null

        override fun addRecord(record: Loggable): Boolean {
            mockRecords.add(record)
            return true
        }
        override fun getRecords(): Collection<Loggable> = mockRecords

    }

    private class FakeHandler() : LogHandler{

        override val targetClassHandled: KClass<out StructuredLoggable> = MockRecord::class
        override val completionSignal: Signal<LogHandler, Unit> = signalOf()

        val records = mutableListOf<StructuredLoggable>()


        override fun processRecord(logRecord: StructuredLoggable) {
            records.add(logRecord)
        }
    }

    @Test
    fun `LogForwarder handler registration and lookup work as expected`(){
        val logProcessor = createLogProcessor()
        val forwarder = LogForwarder()
        val initialMessage = infoMsg("Subject", "Initial message")
        val flow =  logProcessor.createProceduralFlow(initialMessage)
        val handlerRegistration =  forwarder.useHandler(flow)

        assertNotNull(forwarder.getHandlerRegistration(LogMessage::class)){reg->
            assertSame(handlerRegistration.handler, reg.handler)
            assertEquals(3,   reg.hierarchyMap.hierarchyCache.size)
            assertEquals(LogMessage::class,  reg.hierarchyMap.hierarchyCache.first())
            assertEquals(StructuredLoggable::class,  reg.hierarchyMap.hierarchyCache.last())
        }

        val mockedHandler = FakeHandler()
        val  mockedHandlerRegistration =  forwarder.useHandler(mockedHandler)
        assertNotNull(forwarder.getHandlerRegistration(MockRecord::class)) { reg ->
            assertSame(mockedHandlerRegistration.handler, reg.handler)
            assertEquals(3,   reg.hierarchyMap.hierarchyCache.size)
            assertEquals(MockRecord::class,  reg.hierarchyMap.hierarchyCache.first())
            assertEquals(StructuredLoggable::class,  reg.hierarchyMap.hierarchyCache.last())
        }
        forwarder.accessJournal.print()
    }

    @Test
    fun `LogForwarder useHandler with explicit class and lookup work as expected`(){

        val logProcessor = createLogProcessor()
        val forwarder = LogForwarder()
        val initialMessage = infoMsg("Subject", "Initial message")
        val flow =  logProcessor.createProceduralFlow(initialMessage)
        val handlerRegistration =  forwarder.useHandler(flow, LogMessage::class)

        assertNotNull(forwarder.getHandlerRegistration(LogMessage::class)){reg->
            assertSame(handlerRegistration.handler, reg.handler)
            assertEquals(3,   reg.hierarchyMap.hierarchyCache.size)
            assertEquals(LogMessage::class,  reg.hierarchyMap.hierarchyCache.first())
            assertEquals(StructuredLoggable::class,  reg.hierarchyMap.hierarchyCache.last())
        }

        val mockedHandler = FakeHandler()
        val  mockedHandlerRegistration =  forwarder.useHandler(mockedHandler, MockRecord::class)
        assertNotNull(forwarder.getHandlerRegistration(MockRecord::class)) { reg ->
            assertSame(mockedHandlerRegistration.handler, reg.handler)
            assertEquals(3,   reg.hierarchyMap.hierarchyCache.size)
            assertEquals(MockRecord::class,  reg.hierarchyMap.hierarchyCache.first())
            assertEquals(StructuredLoggable::class,  reg.hierarchyMap.hierarchyCache.last())
        }
        forwarder.accessJournal.print()
    }

    @Test
    fun `LogForwarder useHandler  work as expected`(){
        val logProcessor = createLogProcessor()
        val forwarder = LogForwarder()
        val initialMessage = infoMsg("Subject", "Initial message")
        val flow =  logProcessor.createProceduralFlow(initialMessage)
        forwarder.useHandler(flow)
        val message1  = infoMsg("message1", "new text")
        assertTrue { forwarder.handle(message1) }
        assertNotNull( flow.proceduralRecord.logRecords.firstOrNull { it ===  message1} )

        val mockedHandler = FakeHandler()
        val  attachedMockedHandler =  forwarder.useHandler(mockedHandler)
        assertSame(mockedHandler, attachedMockedHandler.handler)
        assertEquals(2, forwarder.handlerRegistrations.size)

        val sourceMsg = infoMsg("mockHandler", "Text")
        val mockRecord = MockRecord(sourceMsg.loggable)

        assertEquals(2, forwarder.handlerRegistrations.size)
        val handled = forwarder.handle(mockRecord)
        assertTrue { handled }
        forwarder.accessJournal.print()
    }

}