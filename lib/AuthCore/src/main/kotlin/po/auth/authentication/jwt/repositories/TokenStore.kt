package po.auth.authentication.jwt.repositories

import po.auth.authentication.jwt.models.JwtToken



/**
 * Interface for storing, resolving, and invalidating tokens by reference ID.
 */
interface TokenStore {
    fun store(sessionID: String, token: String, ttlMillis: Long?): JwtToken
    fun resolve(sessionId: String): JwtToken?
    fun invalidate(sessionId: String)
}

