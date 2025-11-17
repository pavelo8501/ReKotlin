package po.test.misc.types.token

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.toToken
import po.misc.types.token.tokenOf
import po.test.misc.types.TypeTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration


class TestTypeToken: TypeTestBase(), TokenFactory {

    private class GenericClass<T: Any, V: Any>(
        val genericParam:T,
        val genericValue:V,
        val parameter: String = "Parameter String",
    ): TraceableContext


    @Test
    fun `All generic params registered`(){
        val genericClass = GenericClass<String, Int>("String1", 300)
        val token =  tokenOf<GenericClass<String, Int>>()
        assertEquals(2, token.typeSlots.size)
        val first = token.typeSlots.first().kClass
        val second = token.typeSlots[1].kClass
        assertEquals(String::class, first)
        assertEquals(Int::class, second)
    }

    @Test
    fun `Creation by instance produce same result`(){
        val genericClass = GenericClass<String, Int>("String1", 300)
        val token =  genericClass.toToken()
        assertEquals(2, token.typeSlots.size)
        val first = token.typeSlots.first().kClass
        val second = token.typeSlots[1].kClass
        assertEquals(String::class, first)
        assertEquals(Int::class, second)

        val tokenByClass =  tokenOf<GenericClass<String, Int>>()
        assertEquals(token, tokenByClass)
    }

    @Test
    fun `Strict equality check`(){
        val token =  tokenOf<GenericClass<String, Int>>()
        val token2 =  tokenOf<GenericClass<String, Duration>>()

        val forgetClass1 = token as TypeToken<*>
        val forgetClass2 = token2 as TypeToken<*>
        assertEquals(forgetClass1.kClass, forgetClass2.kClass)
        assertFalse { forgetClass1.strictEquality(forgetClass2) }
    }


}