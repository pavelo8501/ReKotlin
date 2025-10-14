package po.test.misc.callback.events

import org.junit.jupiter.api.Test
import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.eventOf
import po.misc.callbacks.events.listenTriggered
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TestEventListeners: EventHost {

    class SomeData(val value: String = "WithSomeValue")
    class ListenerClass()
    val onSomeEvent = eventOf<EventHost, SomeData>(){
        onEvent {

        }
    }

    @Test
    fun `All listeners get notified`(){
        val triggersCount = mutableListOf<SomeData>()
        for(i in 1..10){
            listenTriggered(ListenerClass(), onSomeEvent){
                triggersCount.add(it)
            }
        }
        val data = SomeData()
        onSomeEvent.trigger(data)
        assertEquals(10, triggersCount.size)

        var index = 0
        triggersCount.forEach {
            index ++
            val previousValue =  triggersCount[(index - 1).coerceAtLeast(0)]
            assertEquals(previousValue, it)
        }
    }

    @Test
    fun `Different listeners are independent`() {
        val firstData = mutableListOf<SomeData>()
        val secondData = mutableListOf<SomeData>()

        listenTriggered(ListenerClass(), onSomeEvent) { firstData.add(it) }
        listenTriggered("stringListener", onSomeEvent) { secondData.add(it) }

        val data = SomeData()
        onSomeEvent.trigger(data)

        assertEquals(1, firstData.size)
        assertEquals(1, secondData.size)
        assertSame(firstData.first(), secondData.first())
    }

}