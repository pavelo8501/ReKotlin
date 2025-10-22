package po.test.misc.callback.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.delayed.DelayConfig
import po.misc.callbacks.delayed.DelayWithTicks
import po.misc.callbacks.delayed.DelayedEvent
import po.misc.data.PrettyPrint
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.types.token.TokenFactory
import po.misc.types.token.typeToken
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class TestDelayedEvent: EventHost, TokenFactory {

    class SomeData(
        val value: String = "Time is up"
    ): PrettyPrint{
        override val formattedString: String = value.colorize(Colour.Green)
    }

    private val data = SomeData()

    @Test
    fun `Starting event with no ticks`(){

        var dataTriggered: Any? = null
        val event = DelayedEvent(this@TestDelayedEvent, typeToken<SomeData>() )
        event.onTimer {
            it.output(Colour.Green)
            dataTriggered = it
        }
        runBlocking {
            event.startTimerSuspending(DelayConfig(5.seconds), data)
        }

        assertIs<SomeData>(dataTriggered)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Starting event with no ticks async`() = runTest {

        var dataTriggered: Any? = null
        val event = DelayedEvent(this@TestDelayedEvent, typeToken<SomeData>(), this@runTest)
        event.onTimer {
            it.output(Colour.Green)
            dataTriggered = it
        }
        event.startTimer(DelayConfig(5.seconds), data)
        advanceUntilIdle()
        assertIs<SomeData>(dataTriggered)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Starting event with ticks async`() = runTest {

        var dataTriggered: Any? = null
        val tickTriggers = mutableListOf<SomeData>()
        val event = DelayedEvent(this@TestDelayedEvent, typeToken<SomeData>(), this@runTest)
        event.onTimer {
            it.output(Colour.Green)
            dataTriggered = it
        }

        val tickAction = DelayWithTicks<TestDelayedEvent, SomeData>(15.seconds, 5.seconds){
            it.output(Colour.Yellow)
            tickTriggers.add(it.value)
        }
        event.startTimer(tickAction, data)
        advanceUntilIdle()

        assertIs<SomeData>(dataTriggered)
        assertTrue { tickTriggers.size >= 3 }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Finalization with no final call`() = runTest {

        var dataTriggered: Any? = null
        val tickTriggers = mutableListOf<SomeData>()
        val event = DelayedEvent(this@TestDelayedEvent, typeToken<SomeData>(), this@runTest)

        event.onTimer {
            it.output(Colour.Green)
            dataTriggered = it
        }

        val tickAction = DelayWithTicks<TestDelayedEvent, SomeData>(15.seconds, 5.seconds){
            it.output(Colour.Yellow)
            tickTriggers.add(it.value)
            event.finalizeCounter()
        }
        event.startTimer(tickAction, data)
        advanceUntilIdle()

        assertEquals(1, tickTriggers.size)
        assertNull(dataTriggered)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Finalization before end time`() = runTest {

        var dataTriggered: Any? = null
        val tickTriggers = mutableListOf<SomeData>()
        val event = DelayedEvent(this@TestDelayedEvent, typeToken<SomeData>(), this@runTest)

        event.onTimer {
            it.output(Colour.Green)
            dataTriggered = it
        }

        val tickAction = DelayWithTicks<TestDelayedEvent, SomeData>(15.seconds, 5.seconds){
            it.output(Colour.Yellow)
            tickTriggers.add(it.value)
            event.finalizeCounter(data)
        }
        event.startTimer(tickAction, data)
        advanceUntilIdle()

        assertEquals(1, tickTriggers.size)
        assertNotNull(dataTriggered)
    }

}