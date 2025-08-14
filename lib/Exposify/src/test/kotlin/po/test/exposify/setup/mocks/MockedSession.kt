package po.test.exposify.setup.mocks

import po.auth.extensions.session
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import java.util.UUID


class SessionIdentity(override val sessionID: String, override val remoteAddress: String): SessionIdentified

private val sessionIdentity = SessionIdentity("0", "192.169.1.1")
val mockedSession :  AuthorizedSession = session(sessionIdentity)

val newMockedSession:AuthorizedSession get() = session(SessionIdentity(UUID.randomUUID().toString(), "192.169.1.1"))


internal fun newMockedSession():AuthorizedSession{
    val sessionIdentity = SessionIdentity("0", "192.169.1.1")
   return session(sessionIdentity)
}
