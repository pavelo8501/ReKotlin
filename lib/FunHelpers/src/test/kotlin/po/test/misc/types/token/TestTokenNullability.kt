package po.test.misc.types.token

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.types.castOrThrow
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse


class TestTokenNullability: TokenFactory {


    private class GenericNullableParam<T: Any, V: Any?>(
        val genericParam:T,
        val genericValue:V,
        val parameter: String = "Parameter String",
    ): TraceableContext



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
