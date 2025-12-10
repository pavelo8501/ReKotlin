package po.test.misc.properties

import po.misc.properties.checkType
import po.misc.types.token.TokenFactory
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class TestKPropertyHelpers: TokenFactory {


    val stringProperty: String = "string"
    val listOfStrings: List<String> =listOf("string1", "string2", "string3")
    val listOfIntegers: List<Int> =listOf(1, 2, 3)

    private val listStringProp = TestKPropertyHelpers::listOfStrings

    @Test
    fun `Check return type of KProperty1 correctly checks for list types by TypeToken1`(){
        val stringToken = tokenOf<List<String>>()
        val success = listStringProp.checkType<TestKPropertyHelpers, List<String>>(stringToken)
        assertNotNull(success)

        val intToken = tokenOf<List<Int>>()
        val failure = listStringProp.checkType<TestKPropertyHelpers, List<Int>>(intToken)
        assertNull(failure)
    }

    @Test
    fun `Check return type of KProperty1 correctly checks for list types by TypeToken`(){
        val stringToken = tokenOf<List<String>>()
        val success = listStringProp.checkType<TestKPropertyHelpers, List<String>>(stringToken)
        assertNotNull(success)

        val intToken = tokenOf<List<Int>>()
        val failure = listStringProp.checkType<TestKPropertyHelpers, List<Int>>(intToken)
        assertNull(failure)
    }

    @Test
    fun `Check return type of KProperty1 correctly checks for list types`(){
        val successResult = TestKPropertyHelpers::listOfStrings.checkType<TestKPropertyHelpers, List<String>>()
        assertNotNull(successResult)
        val failedResult = TestKPropertyHelpers::listOfIntegers.checkType<TestKPropertyHelpers, List<String>>()
        assertNull(failedResult)
    }

}

