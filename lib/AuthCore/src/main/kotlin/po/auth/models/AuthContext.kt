package po.auth.models

/**
 * Context passed into authentication provider.
 */
data class AuthenticationContext(
    val headers: Map<String, String>,
    val remoteAddress: String? = null,
    val requestUri: String? = null
)