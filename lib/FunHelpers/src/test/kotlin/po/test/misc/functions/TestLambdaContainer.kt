package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.functions.containers.Adapter
import po.misc.functions.containers.DSLAdapter
import po.misc.functions.containers.DSLProvider
import po.misc.functions.containers.Evaluator
import po.misc.functions.containers.Notifier
import po.misc.functions.containers.NullableProvider
import po.misc.functions.containers.Provider
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestLambdaContainer {

    val controlClass = ControlClass()

    @Test
    fun `NullableProvider dispose work as expected`(){

        val expectedResult =  "Result"
        var triggerCount: Int = 0
        val nullableProvider =  NullableProvider<String>()

        nullableProvider.subscribe {
            triggerCount ++
            expectedResult
        }

        val actualResult1 = nullableProvider.trigger()
        nullableProvider.dispose()
        val actualResult2 = nullableProvider.trigger()

        assertEquals(expectedResult, actualResult1)
        assertEquals(actualResult1, actualResult2)
        assertTrue(triggerCount == 1)

    }


    fun `ResponsiveContainers basic functionality work as expected`() {
        val outputList: MutableList<String> = mutableListOf()
        val producer: Notifier<String> = Notifier { outputList.add(it) }
        for (i in 1..10) {
            producer.trigger("Item/$i")
        }
        assertEquals(10, outputList.size)

        var resultByCallback = 1
        var result = 1
        val provider = Provider<Int>() { 10 }
        provider.resultHandler.onResultProvided { resultByCallback = it }
        result = provider.trigger()

        assertEquals(10, result)
        assertEquals(result, resultByCallback)

        var resultString: String = ""
        val dslProvider = DSLProvider<ControlClass, String>() {
            resultString = controlClass.property1
            controlClass.property1
        }
        dslProvider.trigger(controlClass)
        assertEquals(controlClass.property1, resultString)

        val value = 10
        val evaluator = Evaluator<Int>() { it == 10 }
        var resultBoolean: Boolean = false
        evaluator.resultHandler.onResultProvided { resultBoolean = it }
        evaluator.trigger(value)
        assertTrue(resultBoolean)

        result = 1
        val adapter = Adapter<String, Int>() { it.count() }
        adapter.resultHandler.onResultProvided { result = it }
        adapter.trigger(controlClass.property1)
        assertEquals(controlClass.property1.count(), result)
    }

    @Test
    fun `DSLAdapter work as expected`() {

        data class ParameterClass(val paramString: String = "Param")

        val parameter = ParameterClass()
        val dslAdapter = DSLAdapter<ControlClass, ParameterClass, String>(parameter) { param ->
            property1 + param.paramString
        }
        val result = dslAdapter.trigger(controlClass)
        assertEquals("Property1<String>Param", result)
    }

}