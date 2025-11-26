package po.test.misc.debugging.stack_tracer

import po.misc.context.component.Component
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.NotificationTopic2
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.debugging.stack_tracer.TraceResolver
import po.misc.exceptions.stack_trace.ExceptionTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestTraceResolver : Component {

    private val topic: NotificationTopic2 = NotificationTopic2.Debug
    private var traceResolved: ExceptionTrace? = null
    private val resolver = TraceResolver(this) {
        traceResolved = it
    }
    fun changeCode() {
        topic.changeCode(10)
    }
    override fun notify(logMessage: LogMessage): StructuredLoggable {
        resolver.process(logMessage)
        return logMessage
    }

    @Test
    fun `New topic usage`() {
        changeCode()
        assertEquals(10, topic.code)
        val topic2: NotificationTopic2 = topic.copy(20)
        assertEquals(10, topic.code)
        assertEquals(20, topic2.code)
    }

    @Test
    fun `Stack resolver usage`() {
        resolver.resolveTraceWhen(NotificationTopic.Debug)
        debug("Some subject", "Some text")
        assertNotNull(traceResolved)
    }

}