package po.test.misc.context

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity

class TestCTX {

    abstract class FailedCTXBase() : CTX{

        init {
            identifiedByName
        }
    }
    class FailedCTX() : FailedCTXBase(){

        override val identity: CTXIdentity<FailedCTXBase>  = asIdentity()
    }

    @Test
    fun `Failed context provides meaningful information`(){

        assertDoesNotThrow {
            FailedCTX()
        }
    }
}