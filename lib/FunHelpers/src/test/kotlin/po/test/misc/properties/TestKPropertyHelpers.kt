package po.test.misc.properties

import po.misc.counters.DataRecord
import po.misc.data.output.output
import po.misc.properties.checkType
import po.misc.types.ReflectiveLookup
import po.misc.types.token.TokenFactory
import po.misc.types.token.asList
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class TestKPropertyHelpers: TokenFactory {

    val listOfStrings: List<String> =listOf("string1", "string2", "string3")
    private val listStringProp = TestKPropertyHelpers::listOfStrings

    @Test
    fun `KProperty1 correctly checked for list types by TypeToken`(){
        @Suppress("UNCHECKED_CAST")
        val propertyAsAny = TestKPropertyHelpers::listOfStrings as KProperty1<*, *>
        val token = tokenOf<List<String>>()
        val result = propertyAsAny.checkType<TestKPropertyHelpers, List<String>>(token)
        assertNotNull(result){
            val receivedList = it.get(this)
            assertEquals(3, receivedList.size)
        }
        val receiverToken = tokenOf<TestKPropertyHelpers>()
        val result2 = propertyAsAny.checkType(receiverToken, token)
        assertNotNull(result2){
            val receivedList = it.get(this)
            assertEquals(3, receivedList.size)
        }
        val tokenOfListInt = tokenOf<List<Int>>()
        val failedResult = propertyAsAny.checkType(receiverToken, tokenOfListInt)
        assertNull(failedResult)
    }

    @Test
    fun `KProperty1 correctly checked for list types by TypeToken with conversion`(){
        @Suppress("UNCHECKED_CAST")
        val propertyAsAny = TestKPropertyHelpers::listOfStrings as KProperty1<TestKPropertyHelpers, *>
        val token = tokenOf<String>()
        val failedResult = propertyAsAny.checkType(token)
        assertNull(failedResult)
        val result = propertyAsAny.checkType(token.asList())
        assertNotNull(result){
            val receivedList = it.get(this)
            assertEquals(3, receivedList.size)
        }
    }

    @Test
    fun `Check debug info collector usage`(){
        val token = tokenOf<String>()
        var report: ReflectiveLookup? = null
        listStringProp.checkType(token){
            report = it
        }
        val lookupReport = assertNotNull(report)
        assertFalse(lookupReport.result)
        assertNotNull(report.messages.firstOrNull { it.recordType == DataRecord.MessageType.Info })
        assertNotNull(report.messages.firstOrNull { it.recordType == DataRecord.MessageType.Failure })

        listStringProp.checkType(token.asList()){
            report = it
        }
        assertTrue(report.result)
        assertNotNull(report.messages.firstOrNull { it.recordType == DataRecord.MessageType.Success })
        report.output()
    }

}

