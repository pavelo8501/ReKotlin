package po.auth.authentication.jwt.repositories

import po.auth.authentication.jwt.models.JwtToken
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap


/**
 * Interface for storing, resolving, and invalidating tokens by reference ID.
 */
interface TokenStore {
    fun store(sessionId: String, token: String, ttlMillis: Long? = null)
    fun resolve(sessionId: String): JwtToken?
    fun invalidate(sessionId: String)
}

