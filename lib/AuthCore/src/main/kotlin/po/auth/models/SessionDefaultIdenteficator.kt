package po.auth.models

import po.auth.sessions.interfaces.SessionIdentified

class SessionDefaultIdentity(
    override val sessionID: String = "0", override
    val remoteAddress: String = "0.0.0.0") : SessionIdentified {
}