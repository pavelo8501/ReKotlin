package po.test.misc.callback.events

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.HostedEvent
import po.misc.callbacks.events.event
import po.misc.callbacks.events.eventOf
import po.misc.exceptions.handling.Suspended
import po.misc.functions.NoResult
import po.misc.types.token.TypeToken
import po.test.misc.callback.events.TestCallbackEvent.HostedData
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
        val builtEvent = event{
            onEvent(Suspended) {
                EventResult(it, 10)
            }
        }
        val builtWithTokensEvent = event(TypeToken.create<String>(), TypeToken.create<EventResult>()){
            onEvent(Suspended) {
                EventResult(it, 10)
            }
        }

        val createEvent = eventOf<TestHostedEvent, String, EventResult>()
        val createExplicitParamsEvent : HostedEvent<TestHostedEvent, String, EventResult> = eventOf()
        val createWithTokensEvent = eventOf(TypeToken.create<String>(), TypeToken.create<EventResult>())

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


    @Test
    fun `Parametrized event`() {

        var triggered: Any? = null
        val hosted = HostedData()

        val event =  this.event(NoResult) {
            onEvent {
                triggered = it
            }
        }
        event.trigger(hosted)
    }

    @Test
    fun `triggerBoth triggers both callbacks or synced`() = runTest {

        var triggered: HostedData? = null
        var triggeredBySuspended: Any? = null

        val event =  this@TestHostedEvent.event(NoResult) {
            onEvent {
                triggered = it
            }
            onEvent(Suspended) {
                triggeredBySuspended = it
            }
        }
        val hosted = HostedData()
        event.triggerBoth(hosted, null)
        assertNotNull(triggered)
        assertNotNull(triggeredBySuspended)


        triggered = null
        triggeredBySuspended = null
        val event2 =  this@TestHostedEvent.event(NoResult) {
            onEvent {
                triggered = it
            }
        }
        event2.triggerBoth(hosted, null)
        assertNotNull(triggered)
        assertNull(triggeredBySuspended)

    }
}