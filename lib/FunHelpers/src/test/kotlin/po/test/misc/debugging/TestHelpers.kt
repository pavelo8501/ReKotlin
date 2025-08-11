package po.test.misc.debugging

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.debugging.createDebugFrame

class TestHelpers: CTX {

    override val identity: CTXIdentity<TestHelpers> = asIdentity()

    @Test
    fun `Correct meta created`(){

       val frame = createDebugFrame()

       frame.frameMeta.forEach {
           println(it)
       }

    }
}