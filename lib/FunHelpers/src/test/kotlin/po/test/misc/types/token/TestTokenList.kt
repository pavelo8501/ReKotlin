package po.test.misc.types.token

import po.misc.types.token.TokenOptions
import po.misc.types.token.TypeToken

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTokenList: TokenTestBase() {


    @Test
    fun `Type token writes information about list supertype during creation`(){
        val token = TypeToken<SomeClass>()
        val token2 = TypeToken<SomeClass>(TokenOptions.ListType)
        assertEquals(token, token2)
        assertTrue { token2.isCollection }

    }

}