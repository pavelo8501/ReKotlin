package po.test.misc.types.token

import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.data.output.output
import po.misc.types.token.TokenOptions
import po.misc.types.token.TypeToken
import po.misc.types.token.asElement
import po.misc.types.token.asList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class TestTokenExtensions: TokenTestBase() {

    @Test
    fun `Type token conversion to List`(){

        val token = TypeToken<SomeClass>()
        val listToken = assertDoesNotThrow {
            token.asList()
        }
        assertIs<TypeToken<List<SomeClass>>>(listToken)
        assertNotNull(listToken.typeSlots.firstOrNull()){typeSlot->
            assertEquals(SomeClass::class, typeSlot.kClass)
        }
        assertTrue { listToken.isCollection }
    }

    @Test
    fun `Type token conversion from List to Element`(){
        val token = TypeToken<List<SomeClass>>()
        val elementToken = assertDoesNotThrow {
            token.asElement()
        }
        assertIs<TypeToken<SomeClass>>(elementToken)
        assertEquals(0,elementToken.typeSlots.size)
        assertFalse { elementToken.isCollection }
    }


//    @Test
//    fun `Type token writes information about list supertype during creation`(){
//        val token = TypeToken<SomeClass>()
//        val token2 = TypeToken<SomeClass>(TokenOptions.ListType)
//        assertEquals(token, token2)
//        assertTrue { token2.isCollection }
//    }

}