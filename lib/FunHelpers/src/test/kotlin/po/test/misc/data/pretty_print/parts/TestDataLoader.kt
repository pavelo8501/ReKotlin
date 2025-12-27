package po.test.misc.data.pretty_print.parts

import po.misc.collections.lambda_map.FunctionCallable
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.types.token.TypeToken
import kotlin.test.Test
import kotlin.test.assertEquals

class TestDataLoader {

    val typeToken: TypeToken<TestDataLoader> = TypeToken<TestDataLoader>()
    private val list = listOf("String 1","String 2","String 3","String 4")
    private val text = "String 1"
    private fun listFunction(test:TestDataLoader): List<String> = test.list
    private fun stringFun(test:TestDataLoader): String = "String 1"
    fun someFunResultListInt(): List<Int> = listOf(1,2, 3)
    fun listOfInt(test:TestDataLoader):List<Int> =  listOf(1,2, 3)

    @Test
    fun `add function callables`() {
        val stringProvider = DataProvider(TestDataLoader::list)
        stringProvider.add(FunctionCallable(::listFunction))
        assertEquals(2, stringProvider.callables.size)
        val dataLoader = DataLoader("TestDataLoader", TypeToken<TestDataLoader>(), TypeToken<List<String>>())
        dataLoader.applyCallables(stringProvider)
        assertEquals(2, dataLoader.callables.size)


    }
}