package po.auth.authentication.authenticator.models

import po.auth.sessions.interfaces.SessionIdentified

/**
 * Context passed into authentication provider.
 */
data class AuthenticationData(
    override val sessionId: String,
    override val remoteAddress: String,
    val headers: Map<String, String>,
    val requestUri: String,
    val authHeaderValue: String,
): SessionIdentified