package po.test.misc.types.token

import po.misc.collections.repeatBuild
import po.misc.types.helpers.filterTokenized
import po.misc.types.token.TokenFactory
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertEquals


class TestTokenFilters : TokenFactory{

    interface ClassGroup{
        val parameter: Any
    }
    private class ClassA<T: Any>(
        override val parameter:T,
        override val typeToken: TypeToken<T>
    ): Tokenized<T>, TokenFactory, ClassGroup {
        companion object : TokenFactory{
            inline operator fun <reified T: Any> invoke(parameter:T): ClassA<T>{
               return ClassA(parameter, tokenOf())
            }
        }
    }
    private class ClassB<T: Any>(
        override val parameter:T,
        override val typeToken: TypeToken<T>
    ):Tokenized<T>, ClassGroup{
        companion object : TokenFactory {
            inline operator fun <reified T : Any> invoke(parameter: T): ClassA<T> {
                return ClassA(parameter, tokenOf())
            }
        }
    }

    @Test
    fun `Tokenized filter`() {
        val classAList = 2.repeatBuild { ClassA("Parameter_${it}") }
        val classBList = 4.repeatBuild { ClassB(it) }
        val listOfTokenized = mutableListOf<Tokenized<*>>()
        listOfTokenized.addAll(classAList)
        listOfTokenized.addAll(classBList)

        val result =  listOfTokenized.filterTokenized(ClassA::class,  tokenOf<String>())
        assertEquals(2, result.size)

    }
}