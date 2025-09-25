package po.test.lognotify.setup

import po.auth.extensions.session
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession


class SessionIdentity(override val ip: String, override val userAgent: String): SessionIdentified

private val sessionIdentity = SessionIdentity("0", "192.169.1.1")
val mockedSession :  AuthorizedSession = session(sessionIdentity)


val newMockedSession: AuthorizedSession get() = session(sessionIdentity)




//internal fun newMockedSession():AuthorizedSession{
//    val sessionIdentity = SessionIdentity("0", "192.169.1.1")
//    return session(sessionIdentity)
//}
