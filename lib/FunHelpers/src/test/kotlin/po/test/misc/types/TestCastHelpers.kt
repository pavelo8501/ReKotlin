package po.test.misc.types

import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.types.safeCast
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCastHelpers {

    @Test
    fun `Safe cast does not throw`(){
        val someString = "Some string"
        val asAnny = someString as Any?
        val result = assertDoesNotThrow {
            asAnny?.safeCast<String>()
        }
        assertEquals(someString, result)
    }
}