package po.test.misc.debugging

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.debugging.DebugDispatcher
import po.misc.debugging.createDebugFrame
import po.misc.debugging.identityData

class TestHelpers: CTX {

    override val identity: CTXIdentity<TestHelpers> = asIdentity()

    val debug = DebugDispatcher()

    @Test
    fun `Correct meta created`() {

        val frame = createDebugFrame()

        frame.frameMeta.forEach {
            println(it)
        }
    }

    @Test
    fun `Events of interes can be launched conditionaly `() {
        debug.debugWith(this){
            this.identityData()
        }

    }
}