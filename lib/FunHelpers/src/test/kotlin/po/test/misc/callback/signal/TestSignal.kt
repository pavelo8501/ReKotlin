package po.test.misc.callback.signal

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.tracable.TraceableContext
import po.misc.functions.LambdaOptions
import po.misc.functions.LambdaType
import po.misc.functions.NoResult
import po.misc.functions.SuspendedOptions
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestSignal {

    internal class Data1()

    internal class Listener(): TraceableContext{
        val hash = hashCode()
        var notified: Data1? = null

        val function: (Data1) -> Unit = {
            notified = it
        }
        val resultingFunction: (Data1) -> Int = {
            notified = it
            hash
        }
    }

    internal inline fun <reified R: Any> recreateListeners(signal: Signal<Data1, R>):List<Listener>{
        val listeners = mutableListOf<Listener>()
        for(i in 1..10){
            val listener = Listener()
            @Suppress("Unchecked_cast")
            if(R::class == Unit::class){
                signal.onSignal(listener, listener.function as (Data1) -> R)
            }else{
                signal.onSignal(listener, listener.resultingFunction as (Data1) -> R)
            }
            listeners.add(listener)
        }
        return listeners
    }

    @Test
    fun `Signal simple usage with a listener of its own`(){
        val click = signalOf<Data1>(NoResult)
        var triggered: Any? = null
        click.onSignal  {
            triggered = it
        }
        val  data = Data1()
        click.trigger(data)
        assertIs<Data1>(triggered)
        triggered = null
        val presetEvent = signal<Data1>(NoResult) {
            onSignal {
                triggered = it
            }
        }
        presetEvent.trigger(data)
        assertIs<Data1>(triggered)
    }

    @Test
    fun `Signal usage with a multiple event listeners`(){
        val click = signalOf<Data1>(NoResult)
        val listeners = recreateListeners(click)
        val  data = Data1()
        click.trigger(data)
        assertEquals(10 ,listeners.size)
        listeners.forEach {listener->
            assertIs<Data1>(listener.notified)
        }
    }

    @Test
    fun `Signal usage with  with a multiple event listeners`(){
        val click = signalOf<Data1>(NoResult)
        val listeners = recreateListeners(click)
        val  data = Data1()
        assertEquals(10 ,listeners.size)
        click.trigger(listeners[3], data)
        listeners.take(2).forEach {listener->
            assertNull(listener.notified)
        }
        assertNotNull(listeners.get(3).notified)
        listeners.drop(4).forEach {
            assertNull(it.notified)
        }
    }

    @Test
    fun `Signal with a multiple event listeners and result`(){
        val click = signalOf<Data1, Int>()
        val listeners = recreateListeners(click)
        val data = Data1()
        val results = click.trigger(data)
        results.forEach {result->
           assertNotNull(listeners.firstOrNull{ it.hash ==  result.result} )
        }
    }

    @Test
    fun `Signal with multiple event listeners and single triggered`(){
        val click = signalOf<Data1, Int>()
        val listeners = recreateListeners(click)
        val data = Data1()
        val selectedOne = listeners[5]
        val result = click.trigger(selectedOne, data)
        assertEquals(selectedOne.hash, result)
        listeners.drop(6).forEach {
            assertNull(it.notified)
        }
    }

    @Test
    fun `Signal options work as expected`(){
        val listener1 = Listener()
        val listener2 = Listener()

        val signal = signalOf<Data1>(NoResult)
        var listener1Triggers = 0
        signal.onSignal(listener1, LambdaOptions.Promise){
            listener1Triggers ++
        }

        var listener2Triggers = 0
        signal.onSignal(listener2){
            listener2Triggers ++
        }
        repeat(3){
            val data = Data1()
            signal.trigger(data)
        }
        assertEquals(1, listener1Triggers)
        assertEquals(3, listener2Triggers)
        assertEquals(1, signal.listeners.size)
    }

    @Test
    fun `Signal suspended options work as expected`() = runTest{

        val listener1 = Listener()
        val listener2 = Listener()

        val signal = signalOf<Data1>(NoResult)
        var listener1Triggers = 0
        signal.onSignal(listener1, SuspendedOptions.Promise){
            listener1Triggers ++
        }

        var listener2Triggers = 0
        signal.onSignal(listener2, LambdaType.Suspended){
            listener2Triggers ++
        }
        repeat(3){
            val data = Data1()
            signal.trigger(data, LambdaType.Suspended)
        }
        assertEquals(1, listener1Triggers)
        assertEquals(3, listener2Triggers)
        assertEquals(1, signal.listeners.size)
    }

}