package po.test.misc.collections.lambda_map

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.collections.lambda_map.CallableWrapper
import po.misc.collections.lambda_map.Lambda
import po.misc.collections.lambda_map.LambdaMap
import po.misc.collections.lambda_map.LambdaWithReceiver
import po.misc.collections.lambda_map.toCallable
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.functions.SuspendedOptions
import po.misc.types.safeCast
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        lp.invoke("Something")
        assertEquals("Something", notified)

        val lambda2: TestLambdaMap.(String) -> Unit = {
            notified = it
            it.output()
        }
        val lambdaWithReceiver = LambdaWithReceiver(this, lambda2)
        lambdaWithReceiver.invoke("Something2")
        assertEquals("Something2", notified)
    }

    @Test
    fun `Keys returned as expected`() {

        val function: (String) -> Unit = {
            it.output()
        }
        val lambda = Lambda(function)
        val lambdaMap = LambdaMap<String, Unit>()
        lambdaMap[this] = lambda

        assertTrue(lambdaMap.entries.isNotEmpty())

        val firstEntryKey = assertNotNull(lambdaMap.entries.first().key)
        val firstEntryValue = assertNotNull(lambdaMap.entries.first().value)
        assertEquals(this, firstEntryKey)
        assertSame(this, firstEntryKey)
        assertSame(lambda, firstEntryValue)
    }

    @Test
    fun `Warning issued on key overwrite`() {
        val function: (String) -> Unit = {
            it.output()
        }
        val lambda = Lambda(function)
        val lambdaMap = LambdaMap<String, Unit>()
        var notified: Boolean = false
        lambdaMap.onKeyOverwritten = {
            assertSame(this, it)
            notified = true
        }
        lambdaMap[this] = lambda
        assertFalse { notified }
        lambdaMap[this] = lambda
        assertTrue { notified }
    }

    @Test
    fun `Map usage test with different lambda types`(){
        var notified: String? = null
        val function: (String) -> Unit = {
            notified = it
            it.output()
        }
        val lambda = Lambda(function)
        val lambdaMap = LambdaMap<String, Unit>()
        lambdaMap[this] = lambda

        fun trigger(caller: Any, value: String){
            val mapValue = lambdaMap[caller]
            mapValue?.safeCast<CallableWrapper<String, Unit>>()?.invoke(value)
        }
        trigger(this, "Something2")
        assertEquals("Something2", notified)

        val lambda2: TestLambdaMap.(String) -> Unit = {
            notified = it
            it.output()
        }
        val lambdaWithReceiver = LambdaWithReceiver(this, lambda2)
        lambdaMap[this] = lambdaWithReceiver
        fun trigger2(caller: TraceableContext, value: String){
            val mapValue = lambdaMap[caller]
            mapValue?.safeCast<CallableWrapper<String, Unit>>()?.invoke(value)
        }
        trigger2(this, "with Receiver")
        assertEquals("with Receiver", notified)
    }

    @Test
    fun `Map usage test with suspended lambdas`() = runTest{

        var notified: String? = null
        val suspendedFunction: suspend (String) -> Unit = {
            notified = it
            it.output()
        }
        val lambda = toCallable(SuspendedOptions.Listen, suspendedFunction)
        val lambdaMap = LambdaMap<String, Unit>()
        lambdaMap[this@TestLambdaMap] = lambda

        suspend fun trigger(caller: TraceableContext, value: String){
            val mapValue = lambdaMap[caller]
            mapValue?.invokeSuspending(value)
        }

        trigger(this@TestLambdaMap, "Something2")
        assertEquals("Something2", notified)

        val lambda2: suspend TestLambdaMap.(String) -> Unit = {
            notified = it
            it.output()
        }

        val lambdaWithReceiver = toCallable(lambda2)
        lambdaMap[this@TestLambdaMap] = lambdaWithReceiver
        suspend fun trigger2(caller: TraceableContext, value: String){
            val mapValue = lambdaMap[caller]
            mapValue?.invokeSuspending(value)
        }
        trigger2(this@TestLambdaMap, "with Receiver")
        assertEquals("with Receiver", notified)
    }

    @Test
    fun `All listeners being mapped receive function call`(){

        val lambdaMap = LambdaMap<String, Unit>()
        for(i in 1..10){
            val listener = Listener()
            lambdaMap[listener] = listener.function.toCallable()
        }
        val collectedListeners = mutableListOf<Any>()
        val msg = "Message"
        lambdaMap.values.forEachIndexed {index, mapValue->
            lambdaMap.keys.toList().get(index)?.let {
                collectedListeners.add(it)
            }
            mapValue.invoke("${msg}_$index")
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
        val lambdaMap = LambdaMap<String, Unit>()

        for(i in 1..10){
            val listener = Listener()
            lambdaMap[listener] = listener.function.toCallable()
        }
        val selected = lambdaMap.getCallables<String, Unit>()
        assertEquals(10, selected.size)
        selected.forEach {
            it.invoke("value")
        }
        lambdaMap.keys.forEach {
           val listener = assertIs<Listener>(it)
            assertEquals("value", listener.notified)
        }
    }
}