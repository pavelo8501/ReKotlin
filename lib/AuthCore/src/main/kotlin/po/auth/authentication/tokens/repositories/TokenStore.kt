package po.auth.authentication.tokens.repositories

import kotlin.text.get


/**
 * Interface for storing, resolving, and invalidating tokens by reference ID.
 */
interface TokenStore {
    fun store(refId: String, token: String, ttlMillis: Long? = null)
    fun resolve(refId: String): String?
    fun invalidate(refId: String)
}

/**
 * Default in-memory implementation of TokenStore with optional TTL.
 */
class InMemoryTokenStore : TokenStore {

    private data class StoredToken(val token: String, val expiresAt: Long?)

    private val store = ConcurrentHashMap<String, StoredToken>()

    override fun store(refId: String, token: String, ttlMillis: Long?) {
        val expiresAt = ttlMillis?.let { Instant.now().toEpochMilli() + it }
        store[refId] = StoredToken(token, expiresAt)
    }

    override fun resolve(refId: String): String? {
        val record = store[refId] ?: return null
        if (record.expiresAt != null && Instant.now().toEpochMilli() > record.expiresAt) {
            store.remove(refId)
            return null
        }
        return record.token
    }

    override fun invalidate(refId: String) {
        store.remove(refId)
    }
}

