package po.test.misc.validators

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity


class TestValidator : CTX {

    override val identity: CTXIdentity<out CTX> = asIdentity()

    @Test
    fun `Validator container`(){


    }

}