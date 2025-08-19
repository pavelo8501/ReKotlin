package po.auth.sessions.interfaces

import po.auth.sessions.models.AuthorizedSession


interface SessionIdentified {
    val ip: String
    val userAgent: String
}





