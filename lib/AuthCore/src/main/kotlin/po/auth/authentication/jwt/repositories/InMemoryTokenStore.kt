package po.auth.authentication.jwt.repositories

import po.auth.authentication.jwt.models.JwtToken
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Default in-memory implementation of TokenStore with optional TTL.
 */
class InMemoryTokenStore : TokenStore {

    private data class StoredToken(val token: String, val expiresAt: Long?)

    private val store = ConcurrentHashMap<String, StoredToken>()


     fun store(jwtToken: JwtToken):JwtToken {
         store(jwtToken.sessionId, jwtToken.token)
         return jwtToken
    }

    override fun store(sessionId: String, token: String, ttlMillis: Long?) {
        val expiresAt = ttlMillis?.let { Instant.now().toEpochMilli() + it }
        store[sessionId] = StoredToken(token, expiresAt)
    }

    override fun resolve(sessionId: String): JwtToken? {
        val record = store[sessionId] ?: return null
        if (record.expiresAt != null && Instant.now().toEpochMilli() > record.expiresAt) {
            store.remove(sessionId)
            return null
        }
        return  JwtToken(record.token, sessionId)
    }

    override fun invalidate(sessionId: String) {
        store.remove(sessionId)
    }
}

