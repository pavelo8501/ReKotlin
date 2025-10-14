package po.test.misc.callback.events

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.HostedEvent
import po.misc.callbacks.events.buildHostedEventOf
import po.misc.callbacks.events.createHostedEventOf
import po.misc.exceptions.handling.Suspended
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestHostedEvent: EventHost {

    class EventResult(val triggeredValue: String, val additionalParam: Int)

    private val event = HostedEvent<TestHostedEvent, String, EventResult>(this, TypeToken.create<String>(), TypeToken.create<EventResult>())


    @Test
    fun `Hosted event return results as expected`() = runTest {
        event.onEvent(Suspended) {
            EventResult(it, 5)
        }
        val result = event.trigger("Some Value", Suspended)
        val triggerResult = assertNotNull(result)
        assertEquals(triggerResult.triggeredValue, "Some Value")
        assertEquals(triggerResult.additionalParam, 5)
    }

    @Test
    fun `Hosted event builder work as expected`() {
        val builtEvent = buildHostedEventOf{
            onEvent(Suspended) {
                EventResult(it, 10)
            }
        }
        val builtWithTokensEvent = buildHostedEventOf(TypeToken.create<String>(), TypeToken.create<EventResult>()){
            onEvent(Suspended) {
                EventResult(it, 10)
            }
        }

        val createEvent = createHostedEventOf<TestHostedEvent, String, EventResult>()
        val createExplicitParamsEvent : HostedEvent<TestHostedEvent, String, EventResult> = createHostedEventOf()
        val createWithTokensEvent = createHostedEventOf(TypeToken.create<String>(), TypeToken.create<EventResult>())

        assertEquals(builtEvent.host, createEvent.host)
        assertEquals(builtEvent.paramType, createEvent.paramType)
        assertEquals(builtEvent.resultType, createEvent.resultType)

        assertEquals(builtEvent.host, builtWithTokensEvent.host)
        assertEquals(builtEvent.paramType, builtWithTokensEvent.paramType)
        assertEquals(builtEvent.resultType, builtWithTokensEvent.resultType)

        assertEquals(builtEvent.host, createExplicitParamsEvent.host)
        assertEquals(builtEvent.paramType, createExplicitParamsEvent.paramType)
        assertEquals(builtEvent.resultType, createExplicitParamsEvent.resultType)

        assertEquals(builtEvent.host, createWithTokensEvent.host)
        assertEquals(builtEvent.paramType, createWithTokensEvent.paramType)
        assertEquals(builtEvent.resultType, createWithTokensEvent.resultType)
        

        assertTrue { builtEvent.eventSuspended }
        assertFalse { builtEvent.event }

    }
}