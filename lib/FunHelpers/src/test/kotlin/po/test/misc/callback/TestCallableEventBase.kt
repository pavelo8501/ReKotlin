package po.test.misc.callback

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import po.misc.callbacks.CallableEventBase
import po.misc.callbacks.signal.signal
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCallableEventBase : TraceableContext {

    private val signal1 = signal<String, Unit>{
        signalName = "signal_1"
    }
    private val signal2 = signal<String, Unit>{
        signalName = "signal_2"
    }
    @BeforeEach
    fun teardown(){
        signal1.listenersMap.clear()
        signal2.listenersMap.clear()
    }

    @Test
    fun `EventBase RelayStrategy COPY`(){
        val signalText = "Some text"
        var bySignal1 : String? = null
        signal1.onSignal(this) { bySignal1 = it }
        signal1.relay(signal2, CallableEventBase.RelayStrategy.COPY)
        signal2.trigger(signalText)
        assertEquals(1, signal1.listenersMap.size)
        assertEquals(1, signal2.listenersMap.size)
        assertEquals(signalText, bySignal1)
        assertSame(this, signal1.listenersMap.listenerEntries.first().key)
        assertSame(this, signal2.listenersMap.listenerEntries.first().key)
    }

    @Test
    fun `EventBase RelayStrategy MOVE`(){
        val signalText = "Some text"
        var received : String? = null
        signal1.onSignal(this) { received = it }
        signal1.relay(signal2, CallableEventBase.RelayStrategy.MOVE)
        signal2.trigger(signalText)

        assertEquals("signal_2", signal2.signalName)

        assertEquals(0, signal1.listenersMap.size)
        assertEquals(1, signal2.listenersMap.size)
        assertEquals(signalText, received)
        assertEquals(0, signal1.listenersMap.size)
        assertSame(this, signal2.listenersMap.listenerEntries.first().key)
        signal1.journal.output()
    }

}