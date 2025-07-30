package po.test.exposify.setup.mocks

import po.auth.extensions.session
import po.auth.sessions.models.AuthorizedSession
import po.test.exposify.scope.session.TestSessionsContext



private val sessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
val mockedSession :  AuthorizedSession = session(sessionIdentity)

internal fun newMockedSession():AuthorizedSession{
    val sessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
   return session(sessionIdentity)
}
