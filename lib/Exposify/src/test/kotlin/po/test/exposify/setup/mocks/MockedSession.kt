package po.test.exposify.setup.mocks

import po.auth.extensions.session
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.auth.sessions.models.SessionBase
import java.util.UUID


class SessionIdentity(
    override val ip: String,
    override val userAgent: String,
): SessionIdentified

private val sessionIdentity = SessionIdentity("192.169.1.1", "0")
val mockedSession :  SessionBase = session(sessionIdentity)

val newMockedSession:SessionBase get() = session(SessionIdentity( "192.169.1.1", UUID.randomUUID().toString()))


internal fun newMockedSession():SessionBase{
    val sessionIdentity = SessionIdentity("192.169.1.1", "0")
   return session(sessionIdentity)
}
