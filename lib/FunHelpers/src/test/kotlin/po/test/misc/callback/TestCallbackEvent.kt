package po.test.misc.callback

import org.junit.jupiter.api.Test
import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.createEvent
import po.misc.callbacks.events.createNotification
import kotlin.test.assertIs
import kotlin.test.assertSame


class TestCallbackEvent: EventHost {

    class Hosting(
        val hostingValue: String = "hostingValue"
    ): EventHost{
        val notification = createNotification(TestCallbackEvent::class)
    }

    val testValue: String = "TestValue"

    @Test
    fun `event delegate`(){

        val click = createEvent()
        var triggered: Any? = null

        click.onEvent  {
            triggered = it
        }
        click.trigger()
        assertIs<TestCallbackEvent>(triggered)
    }

    @Test
    fun `notification  delegate`(){
        var triggered: Any? = null
        val host = Hosting()
        host.notification.onEvent {
            triggered = it
        }
        host.notification.trigger(this)
        assertSame(this, triggered)
    }

}