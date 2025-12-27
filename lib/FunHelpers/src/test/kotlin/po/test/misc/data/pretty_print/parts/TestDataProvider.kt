package po.test.misc.data.pretty_print.parts

import org.junit.jupiter.api.assertAll
import po.misc.collections.lambda_map.CallableDescriptor
import po.misc.collections.lambda_map.FunctionCallable
import po.misc.collections.lambda_map.PropertyCallable
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.types.token.TypeToken
import po.misc.types.token.acceptsReceiverOf
import po.misc.types.token.resolvesValueOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDataProvider(){

    private val value: Int  = 100
    private val list = listOf("String 1","String 2","String 3","String 4")

    fun someFunResultInt(): Int = value
    fun someFunResultListInt(): List<Int> = listOf(1,2, 3)
    fun someFunResultByTestInt(test:TestDataProvider): Int = test.value
    fun someFunResultStringList(test:TestDataProvider): List<String> = test.list
    fun listOfInt(test:TestDataProvider):List<Int> =  listOf(1,2, 3)

    @Test
    fun `Data provider successfully store different data sources`(){
        val provider1 = DataProvider(TestDataProvider::list)
        assertTrue { provider1.hasListProperty }
        assertTrue{ provider1.resolvesValueOf<List<String>>() }

        val intProvider = DataProvider(TestDataProvider::value)
        assertAll("DataProvider by fun result int",
            { assertTrue { intProvider.hasProperty } },
            { assertFalse { intProvider.hasListProperty } },
            { assertTrue{ intProvider.resolvesValueOf<Int>() } }
        )
        assertTrue { intProvider.hasProperty }
        assertFalse { intProvider.hasListProperty }
        assertTrue{ intProvider.resolvesValueOf<Int>() }

        val funProvider =  DataProvider(TypeToken<Int>(), ::someFunResultInt)
        assertTrue { funProvider.hasProvider }
        assertTrue{ funProvider.resolvesValueOf<Int>() }

        val funProviderListInt =  DataProvider(TypeToken<List<Int>>(), ::someFunResultListInt)
        assertTrue("hasListProvider is false") { funProviderListInt.hasListProvider }
        funProviderListInt.receiverType.output()
        funProviderListInt.valueType.output()
        assertTrue("Receiver type is not TestDataProvider"){ funProviderListInt.acceptsReceiverOf<List<Int>>() }

        val resolverProviderInt = DataProvider(::someFunResultByTestInt)
        assertTrue { resolverProviderInt.hasResolver }
        assertTrue{ resolverProviderInt.acceptsReceiverOf<TestDataProvider>() }
        assertTrue{ resolverProviderInt.resolvesValueOf<Int>() }

        val resolverProviderList = DataProvider(::someFunResultStringList)
        assertTrue { resolverProviderList.hasListResolver }
        assertTrue{ resolverProviderList.acceptsReceiverOf<TestDataProvider>() }
        assertTrue{ resolverProviderList.resolvesValueOf<List<String>>() }
    }

    @Test
    fun `Callable wrappers saved successfully`(){

        val intProvider = DataProvider(TestDataProvider::value)
        assertNotNull(intProvider.callables.firstOrNull()){callable->
            assertIs<PropertyCallable<TestDataProvider, Int>>(callable)
        }
        val resolverProviderInt = DataProvider(::someFunResultByTestInt)
        assertNotNull(resolverProviderInt.callables.firstOrNull()) { callable ->
            assertIs<FunctionCallable< TestDataProvider, Int>>(callable)
            assertEquals(TestDataProvider::class, callable.receiverType.kClass)
            intProvider.add(callable)
        }
        val resolverFunResultStringList = DataProvider(::someFunResultStringList)
        assertNotNull(resolverFunResultStringList.callables.firstOrNull()) { listResolver ->
            assertIs<FunctionCallable< TestDataProvider, List<String>>>(listResolver)
            assertEquals(TestDataProvider::class, listResolver.receiverType.kClass)
        }
        val intListProvider = DataProvider(::listOfInt)
        assertNotNull(intListProvider.callables.firstOrNull()) { intListCallable ->
            assertIs<FunctionCallable< TestDataProvider, List<Int>>>(intListCallable)
            assertTrue { intListCallable.acceptsReceiverOf<TestDataProvider>() }
            assertTrue { intListCallable.resolvesValueOf<List<Int>>() }
        }
        assertTrue { intListProvider.hasCollection(CallableDescriptor.CallableKey.Resolver) }
        assertFalse { intListProvider.hasCollection(CallableDescriptor.CallableKey.ReadOnlyProperty) }


        assertNotNull(intProvider[CallableDescriptor.CallableKey.ReadOnlyProperty])
        assertNotNull(intProvider[CallableDescriptor.CallableKey.Resolver])
        assertTrue { intProvider.hasResolver }
        assertTrue { intProvider.hasReadOnlyProperty }
        assertFalse { intProvider.hasCollection(CallableDescriptor.CallableKey.Resolver) }

        with(intProvider){

            assertNotNull(intProvider[CallableDescriptor.CallableKey.ReadOnlyProperty])
            assertNotNull(intProvider[CallableDescriptor.CallableKey.Resolver])
            assertTrue { intProvider.hasResolver }
            assertTrue { intProvider.hasReadOnlyProperty }
            assertFalse { intProvider.hasCollection(CallableDescriptor.CallableKey.Resolver) }
        }
        assertNotNull(resolverFunResultStringList.callables.firstOrNull()) { listResolver ->
            assertIs<FunctionCallable<TestDataProvider, List<String>>>(listResolver)
            assertEquals(TestDataProvider::class, listResolver.receiverType.kClass)
           with(resolverFunResultStringList){
               assertNotNull(intProvider[CallableDescriptor.CallableKey.Resolver])
           }
        }
    }
}