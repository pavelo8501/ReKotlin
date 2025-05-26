package po.auth.sessions.interfaces

import po.auth.sessions.models.AuthorizedSession

interface SessionIdentified {
    val sessionID: String
    val remoteAddress: String

}

