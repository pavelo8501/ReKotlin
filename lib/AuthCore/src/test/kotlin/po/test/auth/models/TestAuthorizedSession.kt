package po.test.auth.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.auth.sessions.interfaces.SessionHolder
import po.auth.sessions.interfaces.scopedSession
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.logging.ContextAware
import po.misc.data.logging.logEmitter

class TestAuthorizedSession: SessionHolder, ContextAware {

    override val identity: CTXIdentity<TestAuthorizedSession> = asIdentity()
    override val session = scopedSession()
    override val emitter = logEmitter()


    @Test
    fun `Scoped session creation does not throw`(){

        assertDoesNotThrow {
            info("msg")
        }
    }

}