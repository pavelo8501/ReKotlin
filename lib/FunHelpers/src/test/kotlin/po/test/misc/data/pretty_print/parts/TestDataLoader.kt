package po.test.misc.data.pretty_print.parts

import po.misc.callbacks.callable.CallableRepositoryHub
import po.misc.callbacks.callable.asFunCallable
import po.misc.callbacks.callable.asPropertyCallable
import po.misc.callbacks.callable.asProvider
import po.misc.collections.repeatBuild
import po.misc.data.pretty_print.parts.loader.ElementProvider
import po.misc.data.pretty_print.parts.loader.ListProvider
import po.misc.data.pretty_print.parts.loader.createProvider
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.loader.toListProvider
import po.misc.functions.CallableKey
import po.misc.types.token.resolvesValueOf
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDataLoader : PrettyTestBase(), CallableRepositoryHub<TestDataLoader, String> {

    private val listOfStrings = listOf("String 1","String 2")
    private fun listFunction(test:TestDataLoader): List<String> = listOfStrings

    private val string = "String Prop"
    private fun stringFun(test:TestDataLoader): String = "String Fun"

    override val elementRepository: ElementProvider<TestDataLoader, String> = TestDataLoader::string.toElementProvider()
    override val listRepository: ListProvider<TestDataLoader, String> = TestDataLoader::listOfStrings.toListProvider()

    @Test
    fun `ElementProvider with different types for props`(){
        val listProvider =  TestDataLoader::listOfStrings.toListProvider()
        assertTrue { listProvider.receiverType.isCollection }
        assertEquals(TestDataLoader::class, listProvider.sourceType.kClass)
        assertEquals(String::class, listProvider.receiverType.effectiveClass)
        assertTrue { listProvider.resolvesValueOf<List<String>>() }
        assertEquals(1, listProvider.size)
        assertNotNull(listProvider[CallableKey.Property]){
            assertTrue { it.isCollection }
        }

        val listFunctionCallable =  asFunCallable(::listFunction)
        assertTrue { listFunctionCallable.isCollection }
        assertEquals(TestDataLoader::class, listFunctionCallable.sourceType.kClass)
        assertEquals(String::class, listFunctionCallable.receiverType.effectiveClass)
        assertTrue { listFunctionCallable.isCollection }
        assertTrue { listFunctionCallable.resolvesValueOf<List<String>>() }
        listProvider.add(listFunctionCallable)

        val stringFunctionCallable =  asFunCallable(::stringFun)

        assertEquals(TestDataLoader::class, stringFunctionCallable.sourceType.kClass)
        assertEquals(String::class, stringFunctionCallable.receiverType.effectiveClass)
        assertFalse { stringFunctionCallable.isCollection }
        assertTrue { stringFunctionCallable.resolvesValueOf<String>() }

        val stringProvider = stringFunctionCallable.createProvider()
        assertEquals(1, stringProvider.size)
        assertNotNull(stringProvider[CallableKey.Resolver]){
            assertFalse { it.isCollection }
        }
        val stringProperty =  TestDataLoader::string.asPropertyCallable()
        assertEquals(TestDataLoader::class, stringProperty.sourceType.kClass)
        assertEquals(String::class, stringProperty.receiverType.effectiveClass)
        assertFalse { stringProperty.isCollection }
        assertTrue { stringFunctionCallable.resolvesValueOf<String>() }
        stringProvider.add(stringProperty)
        assertEquals(2, stringProvider.size)
    }

    @Test
    fun `Call methods of repositories`(){

        assertTrue { canResolve }
        val result = elementRepository.call(this)
        elementRepository.add(asFunCallable(::stringFun))
        assertEquals(string, result, "Property was not selected as a priority source")

        val listResult = elementRepository.callAll(this)
        assertEquals(2, listResult.size)
        assertEquals(string, listResult[0])
        assertEquals(stringFun(this), listResult[1])

        listRepository.add(asFunCallable(::listFunction))
        val listFunResult = listRepository.call(this)
        assertEquals(4, listFunResult.size)
        val joinedResult = resolveAll(this)
        assertEquals(6, joinedResult.size)
        
        clearRepositories()
        assertFalse { canResolve }
    }

    @Test
    fun `Call methods of resolver`(){
        val someString = "Some string"
        val provider1 =  someString.asProvider()
        assertEquals(String::class, provider1.receiverType.kClass)
        assertEquals(String::class, provider1.receiverType.kClass)
        val callResults =  6.repeatBuild { provider1.call() }

        assertEquals(6, callResults.size)
        assertEquals("Some string", callResults[5])
        val provider2 =  asProvider { 300 }
        assertEquals(TestDataLoader::class, provider2.sourceType.kClass)
        assertEquals(Int::class, provider2.receiverType.kClass)
        val callResults2 =  3.repeatBuild { provider2.call() }
        assertEquals(3, callResults2.size)
        assertEquals(300, callResults2[1])
    }
}