package po.test.misc.collections.lambda_map

import org.junit.jupiter.api.Test
import po.misc.collections.lambda_map.Lambda
import po.misc.collections.lambda_map.LambdaMap
import po.misc.collections.lambda_map.LambdaWithReceiver
import po.misc.collections.lambda_map.LambdaWrapper
import po.misc.collections.lambda_map.toCallable
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestLambdaMap: Component {

    internal class Listener(): TraceableContext{
        var notified: String? = null
        val function: (String) -> Unit = {
            notified = it
        }
    }

    @Test
    fun `LambdaPayload can be used as a value for a map`() {
        var notified: String? = null
        val lambda: (String) -> Unit = {
            notified = it
            it.output()
        }
        val lp = Lambda(lambda)
        lp.invoke("Something", Unit)
        assertEquals("Something", notified)

        val lambda2: TestLambdaMap.(String) -> Unit = {
            notified = it
            it.output()
        }
        val lambdaWithReceiver = LambdaWithReceiver(this, lambda2)
        lambdaWithReceiver.invoke(this, "Something2")
        assertEquals("Something2", notified)
    }

    @Test
    fun `Keys returned as expected`() {

        val function: (String) -> Unit = {
            it.output()
        }
        val lambda = Lambda(function)
        val lambdaMap = LambdaMap<String, Unit,  Unit>(this)
        lambdaMap[this] = lambda

        assertTrue(lambdaMap.listenerEntries.isNotEmpty())

        val firstEntryKey = assertNotNull(lambdaMap.listenerEntries.first().key)
        val firstEntryValue = assertNotNull(lambdaMap.listenerEntries.first().value)
        assertEquals(this, firstEntryKey)
        assertSame(this, firstEntryKey)
        assertSame(lambda, firstEntryValue)
    }

    @Test
    fun `All listeners being mapped receive function call`(){

        val lambdaMap = LambdaMap<String, Unit, Unit>(this)
        for(i in 1..10){
            val listener = Listener()
            lambdaMap[listener] =  Lambda(listener.function)
        }
        val collectedListeners = mutableListOf<Any>()
        val msg = "Message"
        lambdaMap.lambdaMap.values.forEachIndexed {index, mapValue->
            lambdaMap.lambdaMap.toList()[index].let {
                collectedListeners.add(it)
            }
            mapValue.call("${msg}_$index", Unit)
        }
        assertEquals(10, collectedListeners.size)
        collectedListeners.forEachIndexed {index, listener->
            val collectedListener = assertIs<Listener>(listener)
            assertTrue {
                collectedListener.notified?.contains("Message")?:false  &&
                        collectedListener.notified?.contains(index.toString())?:false
            }
        }
    }

    @Test
    fun `Maps get all functionality`(){
        val lambdaMap = LambdaMap<String, Unit, Unit>(this)

        for(i in 1..10){
            val listener = Listener()
            lambdaMap[listener] =   Lambda(listener.function)
        }
        val selected = lambdaMap.getCallables<String, Unit>()
        assertEquals(10, selected.size)
        selected.forEach {
            if(it is LambdaWrapper){
                it.call("value", Unit)
            }
        }
        lambdaMap.listenerEntries.forEach {
           val listener = assertIs<Listener>(it)
            assertEquals("value", listener.notified)
        }
    }
}