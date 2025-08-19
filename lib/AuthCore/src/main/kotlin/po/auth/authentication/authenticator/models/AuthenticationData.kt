package po.auth.authentication.authenticator.models

import po.auth.sessions.interfaces.SessionIdentified


/**
 * Context passed into authentication provider.
 */
data class AuthenticationData(
    override val ip: String,
    override val userAgent: String,
    val headers: Map<String, String>,
    val requestUri: String,
    val authHeaderValue: String,
): SessionIdentified