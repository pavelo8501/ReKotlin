package po.test.misc.types.token

import po.misc.types.safeCast
import po.misc.types.token.TokenHolder
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class TestTokenBasedCasts : TokenTestBase() {

    class Holder<T, V> (
        val paramT:T,
        val paramV:V,
        override val typeToken: TypeToken<*>,
        val parameterVToken:TypeToken<V>
    ): TokenHolder{
        override val types : List<TypeToken<*>> get() = listOf(typeToken, parameterVToken)
    }
    class TokenizedClass<T> (
        val paramT:T,
        override val typeToken: TypeToken<T>,
    ): Tokenized<T>{
    }

    private val tokenString = tokenOf<String>()
    private val tokenInt = tokenOf<Int>()

//    @Test
//    fun `Token based safe cast test`(){
//        val parametrized = ParametrizedClass("String type parameter")
//        val asAny = parametrized as Any
//        val result =  asAny.safeCast<ParametrizedClass<String>, String>(ParametrizedClass::class,  tokenString)
//        assertNotNull(result)
//
//        val parametrized2 = DoubleParametrized("String type parameter", 100)
//        val asAny2 = parametrized2 as Any
//        val result2 =   asAny2.safeCast<DoubleParametrized<*, Int>, Int>(tokenInt)
//        assertNotNull(result2)
//    }

//    @Test
//    fun `Token holder based safe cast test`(){
//        val parametrized = Holder("String type parameter", 100,tokenOf<String>(), tokenOf<Int>())
//        val asTokenHolder = parametrized as TokenHolder
//        val result =  asTokenHolder.safeCast<Holder<*, Int>, Int>(tokenInt)
//        assertNotNull(result)
//        val result2 =  asTokenHolder.safeCast<Holder<String, Int>, String>(tokenString)
//        assertNotNull(result2)
//
//        val parametrized2 = Holder("String type parameter", true, tokenOf<String>(), tokenOf<Boolean>())
//        val asTokenHolder2 = parametrized2 as TokenHolder
//        val result3 =  asTokenHolder2.safeCast<Holder<String, Int>>(tokenString)
//        assertNotNull(result3)
//        val failedResult = asTokenHolder2.safeCast<Holder<String, Int>>(tokenInt)
//        assertNull(failedResult)
//    }

    @Test
    fun `Tokenized  based safe cast test`(){
        val tokenized = TokenizedClass("String type parameter", tokenOf<String>())
        val asTokenized = tokenized as Tokenized<*>
        val result =  asTokenized.safeCast<TokenizedClass<String>, String>()
        assertNotNull(result)

        val failedResult =  asTokenized.safeCast<TokenizedClass<Int>, Int>()
        assertNull(failedResult)
    }
}