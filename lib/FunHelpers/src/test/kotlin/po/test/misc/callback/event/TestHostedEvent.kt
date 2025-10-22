package po.test.misc.callback.event

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.HostedEvent
import po.misc.callbacks.event.event
import po.misc.callbacks.event.eventOf
import po.misc.callbacks.event.infoScope
import po.misc.callbacks.event.listen
import po.misc.context.tracable.TraceableContext
import po.misc.functions.NoResult
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHostedEvent: EventHost {

    internal class DataH()

    internal class Listener(): TraceableContext{
        val hash = hashCode()
        var notified: DataH? = null


        val function: TestHostedEvent.(DataH) -> Unit = {
            notified = it
        }
        val resultingFunction: TestHostedEvent.(DataH) -> Int = {
            notified = it
            hash
        }
    }
    internal inline fun <reified R: Any> recreateListeners(event: HostedEvent<TestHostedEvent,  DataH, R>):List<Listener>{

        val listeners = mutableListOf<Listener>()
        for(i in 1..10){
            val listener = Listener()
            @Suppress("Unchecked_cast")
            if(R::class == Unit::class){
                event.onEvent(listener, listener.function as TestHostedEvent.(DataH) -> R)
            }else{
                event.onEvent(listener, listener.resultingFunction as TestHostedEvent.(DataH) -> R)
            }
            listeners.add(listener)
        }
        return listeners
    }
    var triggered: Any? = null

    @BeforeEach
    fun clear(){
        triggered = null
    }

    @Test
    fun `HostedEvent simple usage with a listener of its own`(){

        val click = eventOf<TestHostedEvent, DataH>(NoResult)
        click.onEvent  {
            triggered = it
        }
        val  data = DataH()
        click.trigger(data)
        assertIs<DataH>(triggered)

        triggered = null
        val presetEvent = event<TestHostedEvent, DataH>(NoResult) {
            onEvent {
                triggered = it
            }
        }
        presetEvent.trigger(data)
        assertIs<DataH>(triggered)
    }

    @Test
    fun `HostedEvent usage with a multiple event listeners`(){
        val click = eventOf<TestHostedEvent, DataH>(NoResult)
        val listeners = recreateListeners(click)
        val  data = DataH()
        click.trigger(data)
        assertEquals(10 ,listeners.size)
        listeners.forEach {listener->
            assertIs<DataH>(listener.notified)
        }
    }

    @Test
    fun `HostedEvent attached functions and other perks work as expected`(){
        val click = eventOf<TestHostedEvent, DataH>(NoResult, "click")
        click.infoScope("HostedEvent attached functions and other perks work as expected"){
            proceduralStep("Lambda initialized"){
                listen(click){
                    triggered = it
                }
            }
            proceduralStep("Lambda Invocation"){
                click.trigger(DataH())
            }
            proceduralStep("Calculating result"){
                assertNotNull(triggered)
            }
        }
        assertNotNull(triggered, "Control assertion")
    }

    @Test
    fun `HostedEvent usage with  with a multiple event listeners`(){
        val click = eventOf<TestHostedEvent, DataH>(NoResult)
        val listeners = recreateListeners(click)
        val  data = DataH()
        assertEquals(10 ,listeners.size)
        click.trigger(listeners[3], data)
        listeners.take(2).forEach {listener->
            assertNull(listener.notified)
        }
        assertNotNull(listeners[3].notified)
        listeners.drop(4).forEach {
            assertNull(it.notified)
        }
    }

    @Test
    fun `HostedEvent with a multiple event listeners and result`(){
        val click = eventOf<TestHostedEvent, DataH, Int>()
        val listeners = recreateListeners(click)
        val data = DataH()
        val results = click.trigger(data)
        results.forEach {result->
            assertNotNull(listeners.firstOrNull{ it.hash ==  result.result} )
        }
    }

    @Test
    fun `HostedEvent with multiple event listeners and single triggered`(){
        val click = eventOf<TestHostedEvent, DataH, Int>()
        val listeners = recreateListeners(click)
        val data = DataH()
        val selectedOne = listeners[5]
        val result = click.trigger(selectedOne, data)
        assertEquals(selectedOne.hash, result)
        listeners.drop(6).forEach {
            assertNull(it.notified)
        }
    }

}