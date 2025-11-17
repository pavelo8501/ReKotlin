package po.test.misc.types.token

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.types.castOrThrow
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.nullable
import po.misc.types.token.tokenOf
import po.test.misc.types.token.TestTypeToken.GenericClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration


class TestTokenNullability: TokenFactory {


    private class GenericNullableParam<T: Any, V: Any?>(
        val genericParam:T,
        val genericValue:V,
        val parameter: String = "Parameter String",
    ): TraceableContext


    @Test
    fun `Nullable type token creation`(){
        val token = TypeToken.create<String>().nullable()
        assertTrue {
            token.isNullable
        }
        assertTrue {
            token.equals(String::class)
        }
        val stringType: String = ""
        val erasedType = (stringType   as Any)
        val restored = assertDoesNotThrow {
            erasedType.castOrThrow(token.kClass)
        }
        assertIs<String>(restored)
        token.typeName.output()
    }

    @Test
    fun `Tokens  respects generic param nullability`(){

        val token = tokenOf<GenericNullableParam<Int, String?>>()
        token.output()
        val token2 =  tokenOf<GenericNullableParam<Int, String>>()
        token2.output()

        val forgetClass1 = token as TypeToken<*>
        val forgetClass2 = token2 as TypeToken<*>
        assertEquals(forgetClass1.kClass, forgetClass2.kClass)
        assertFalse { forgetClass1.strictEquality(forgetClass2) }
    }

}
