package po.restwraptor.calls

import po.auth.sessions.interfaces.SessionIdentified

data class CallerInfo(
    override val ip: String,
    override val userAgent: String,
    val route: String,
    val headers: Map<String, String>
): SessionIdentified
