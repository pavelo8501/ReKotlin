package po.test.misc.callback.callable

import po.misc.callbacks.callable.CallableRepository
import po.misc.data.output.output
import po.misc.functions.CallableKey
import po.misc.types.token.acceptsReceiverOf
import po.misc.types.token.resolvesValueOf
import po.test.misc.data.pretty_print.parts.TestDataLoader
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.collections.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestCallableRepository {


    private val list = listOf("String 1","String 2","String 3","String 4")
    private val text = "String 1"
    private fun listFunction(test:TestCallableRepository): List<String> = list
    val stringProperty: String = "String"
    val listOfRecords : List<PrintableRecord> get() {
        return listOf(PrintableRecord(), PrintableRecord())
    }




    @Test
    fun `RepoInheritor with different types for props`() {

        val callable = CallableRepository(TestCallableRepository::listOfRecords)

//
//
//        val callable2 =  propertyCallable(TestDataLoader::stringProperty)
//
//        val inheritor1 =  propertyAsListCallable(TestDataLoader::listOfRecords2)
//
//        assertEquals(PrintableRecord::class, callable.valueType.effectiveClass)
//        assertTrue { callable.valueType.isCollection }
//        assertEquals(String::class, callable2.valueType.kClass)
//        assertEquals(PrintableRecord::class, inheritor1.valueType.effectiveClass)
//        assertTrue { inheritor1.valueType.isCollection }
//        assertTrue { inheritor1.resolvesValueOf<List<PrintableRecord>>() }
//        assertTrue { inheritor1.acceptsReceiverOf<TestDataLoader>() }
//
//        callable2.joinedList.output()
//
//        assertNotNull(callable2[CallableKey.Property], "Property no found by key"){
//            val res =  it.call(this)
//            assertEquals(stringProperty, res)
//        }
//        assertNotNull(inheritor1[CallableKey.Property]){
//            val res =  it.call(this)
//            assertEquals(2, res.size)
//            assertEquals(listOfRecords2[0].name, res[0].name)
//        }
    }


    @Test
    fun `RepoInheritor with different types for functions`() {

//        val funcCallable = funcCallable(::listOfInt)
//        assertEquals(Int::class, funcCallable.valueType.effectiveClass)
//        assertEquals(TestDataLoader::class, funcCallable.receiverType.effectiveClass)
//        assertEquals(Int::class, funcCallable.valueType.effectiveClass)
//        assertTrue { funcCallable.valueType.isCollection }
//        assertTrue { funcCallable.resolvesValueOf<List<Int>>() }
//        assertTrue { funcCallable.acceptsReceiverOf<TestDataLoader>() }
//
//        val funcAsListCallable = funcAsListCallable(::listOfInt)
//        assertEquals(Int::class, funcCallable.valueType.effectiveClass)
//        assertEquals(TestDataLoader::class, funcCallable.receiverType.effectiveClass)
//        assertTrue { funcAsListCallable.valueType.isCollection }
//        assertTrue { funcAsListCallable.resolvesValueOf<List<Int>>() }
//        assertTrue { funcAsListCallable.acceptsReceiverOf<TestDataLoader>() }
    }

}