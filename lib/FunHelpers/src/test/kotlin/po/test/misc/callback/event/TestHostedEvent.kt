package po.test.misc.callback.event

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.HostedEvent
import po.misc.callbacks.event.event
import po.misc.callbacks.event.eventOf
import po.misc.callbacks.event.listen
import po.misc.callbacks.signal.signalOf
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.functions.LambdaOptions
import po.misc.functions.NoResult
import po.misc.functions.SuspendedOptions
import po.test.misc.callback.signal.TestSignal.Data1
import po.test.misc.callback.signal.TestSignal.Listener
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHostedEvent: EventHost {

    internal class Data()

    internal class Listener(): TraceableContext{
        val hash = hashCode()
        var notified: Data? = null


        val function: TestHostedEvent.(Data) -> Unit = {
            notified = it
        }
        val resultingFunction: TestHostedEvent.(Data) -> Int = {
            notified = it
            hash
        }
    }
    internal inline fun <reified R: Any> recreateListeners(event: HostedEvent<TestHostedEvent,  Data, R>):List<Listener>{

        val listeners = mutableListOf<Listener>()
        for(i in 1..10){
            val listener = Listener()
            @Suppress("Unchecked_cast")
            if(R::class == Unit::class){
                event.onEvent(listener, listener.function as TestHostedEvent.(Data) -> R)
            }else{
                event.onEvent(listener, listener.resultingFunction as TestHostedEvent.(Data) -> R)
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

        val click = eventOf<TestHostedEvent, Data>(NoResult)
        click.onEvent  {
            triggered = it
        }
        val  data = Data()
        click.trigger(data)
        assertIs<Data>(triggered)

        triggered = null
        val presetEvent = event<TestHostedEvent, Data>(NoResult) {
            onEvent {
                triggered = it
            }
        }
        presetEvent.trigger(data)
        assertIs<Data>(triggered)
    }

    @Test
    fun `HostedEvent usage with a multiple event listeners`(){
        val click = eventOf<TestHostedEvent, Data>(NoResult)
        val listeners = recreateListeners(click)
        val  data = Data()
        click.trigger(data)
        assertEquals(10 ,listeners.size)
        listeners.forEach {listener->
            assertIs<Data>(listener.notified)
        }
    }

    @Test
    fun `HostedEvent usage with  with a multiple event listeners`(){
        val click = eventOf<TestHostedEvent, Data>(NoResult)
        val listeners = recreateListeners(click)
        val  data = Data()
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
        val click = eventOf<TestHostedEvent, Data, Int>()
        val listeners = recreateListeners(click)
        val data = Data()
        val results = click.trigger(data)
        results.forEach {result->
            assertNotNull(listeners.firstOrNull{ it.hash ==  result.result} )
        }
    }

    @Test
    fun `HostedEvent with multiple event listeners and single triggered`(){
        val click = eventOf<TestHostedEvent, Data, Int>()
        val listeners = recreateListeners(click)
        val data = Data()
        val selectedOne = listeners[5]
        val result = click.trigger(selectedOne, data)
        assertEquals(selectedOne.hash, result)
        listeners.drop(6).forEach {
            assertNull(it.notified)
        }
    }

    @Test
    fun `HostedEvent named lambdas`() = runTest{

        val listener1 = Listener()
        val listener2 = Listener()
        val event = eventOf<TestHostedEvent, Data>(NoResult)
        val promise = SuspendedOptions.Promise
        promise.name = "Named Promise"
        event.onEvent(listener1, promise){

        }
        event.onEvent(listener2, LambdaOptions.Listen){

        }
        val namedPromise = assertNotNull( event.listenersMap.listeners.first { it.options ==   SuspendedOptions.Promise} )
        val generatedListen = assertNotNull( event.listenersMap.listeners.first { it.options ==  LambdaOptions.Listen } )
        assertTrue {
            namedPromise.lambdaName.contains(promise.name?:"Failure") &&
                    generatedListen.lambdaName.contains("HostedEvent named lambdas")
        }
        namedPromise.lambdaName.output(Colour.Yellow)
        generatedListen.lambdaName.output(Colour.Cyan)
    }

}