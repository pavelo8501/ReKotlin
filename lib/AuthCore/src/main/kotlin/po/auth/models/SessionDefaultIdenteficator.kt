package po.auth.models

import po.auth.sessions.interfaces.SessionIdentified

class SessionDefaultIdentity(
    override val ip: String = "0.0.0.0",
    override val userAgent: String = "") : SessionIdentified {
}