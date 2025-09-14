package po.test.misc.data.logging

import org.junit.jupiter.api.Test
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.helpers.output
import po.misc.data.logging.ContextAware
import po.misc.data.logging.ContextAwareLogEmitter
import po.misc.data.logging.logEmitter
import po.test.misc.setup.captureOutput
import kotlin.test.assertTrue

class TestContextAware: ContextAware {

    override val identity: CTXIdentity<TestContextAware> = asIdentity()
    override val emitter: ContextAwareLogEmitter = logEmitter()
    init {
        identity.setNamePattern {
            "TestContextAware(Something)"
        }
    }

    @Test
    fun `ContextAwareLogEmitter messages contain class related information`() {
       val captured = captureOutput {
            info("Some message")
       }
       captured.output()
       assertTrue(captured.contains("TestContextAware"))
    }

    @Test
    fun `ContextAwareLogEmitter warnings include method name`() {

        val captured = captureOutput {
            warn("Some warning")
        }
        captured.output()
        assertTrue(captured.contains("TestContextAware"))
    }
}