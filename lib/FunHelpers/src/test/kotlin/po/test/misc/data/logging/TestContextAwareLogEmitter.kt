package po.test.misc.data.logging

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.logging.ContextAware
import po.misc.data.logging.ContextAwareLogEmitter
import po.misc.data.logging.logEmitter
import po.misc.data.logging.models.ContextMessage
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestContextAwareLogEmitter: ContextAware {

    override val identity: CTXIdentity<out CTX> = asIdentity()
    var lastDebugMessage: ContextMessage? = null

    override val emitter: ContextAwareLogEmitter = logEmitter{
        onMessage {
            lastDebugMessage = it
        }
    }

    @Test
    fun `ContextAwareLogEmitter debug message`(){

        val methodName = "ContextAwareLogEmitter debug message"
        debug(methodName, "Debug message")
        val debugMsg = assertNotNull(lastDebugMessage)

        assertEquals(methodName, debugMsg.methodName)
        assertEquals("Debug message", debugMsg.message)

    }

}