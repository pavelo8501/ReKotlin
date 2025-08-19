package po.test.exposify.setup.mocks

import po.auth.extensions.session
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import java.util.UUID


class SessionIdentity(
    override val ip: String,
    override val userAgent: String,
): SessionIdentified

private val sessionIdentity = SessionIdentity("192.169.1.1", "0")
val mockedSession :  AuthorizedSession = session(sessionIdentity)

val newMockedSession:AuthorizedSession get() = session(SessionIdentity( "192.169.1.1", UUID.randomUUID().toString()))


internal fun newMockedSession():AuthorizedSession{
    val sessionIdentity = SessionIdentity("192.169.1.1", "0")
   return session(sessionIdentity)
}
