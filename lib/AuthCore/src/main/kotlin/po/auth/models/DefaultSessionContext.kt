package po.auth.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.auth.interfaces.UserSession
import kotlin.coroutines.CoroutineContext

/**
 * Default coroutine-aware session implementation.
 */
class DefaultSessionContext(
    override val userId: Long,
    val username: String,
    val roles: Set<String> = emptySet(),
    override val sessionId: String = generateSessionId(),
    override val createdAt: Long = System.currentTimeMillis()
) : UserSession, CoroutineScope {

    private val attributes = mutableMapOf<String, Any?>()

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO + this

    override fun <T> setAttribute(key: String, value: T) {
        attributes[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getAttribute(key: String): T? = attributes[key] as? T

    companion object {
        private fun generateSessionId(): String = java.util.UUID.randomUUID().toString()
    }
}