package po.auth.authentication.jwt.repositories

import po.auth.authentication.jwt.models.JwtToken
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Default in-memory implementation of TokenStore with optional TTL.
 */
class InMemoryTokenStore : TokenStore {


    private val store = ConcurrentHashMap<String, JwtToken>()


    override fun store(sessionID: String, token: String, ttlMillis: Long?): JwtToken {
        val expiresAt = ttlMillis?.let { Instant.now().toEpochMilli() + it }
        val jwtToken = JwtToken(sessionID, token)
        store[sessionID] = jwtToken
        return jwtToken
    }

    override fun resolve(sessionId: String): JwtToken? {
        println("Requesting session with id $sessionId")
      return  store[sessionId] ?:run{
            println("Session with id $sessionId can not be found. Total store count ${store.values.count()}")
            return null
        }

    }

    override fun invalidate(sessionId: String) {
        store.remove(sessionId)
    }
}

