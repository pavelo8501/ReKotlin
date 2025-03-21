package po.exposify.scope.session

import po.exposify.scope.session.interfaces.UserSession
import kotlin.coroutines.coroutineContext

/**
 * Shortcut to access the current session in suspend blocks.
 */
suspend fun currentSession(): UserSession? = coroutineContext[UserSession]

suspend fun requireSession(): UserSession =
    currentSession() ?: error("No session available in current coroutine context")

/**
 * Default in-memory implementation of SessionContext.
 */
class DefaultSessionContext(
    override val userId: Long?,
    override val sessionId: String = generateSessionId(),
    override val createdAt: Long = System.currentTimeMillis()
) : UserSession {
    private val attributes = mutableMapOf<String, Any?>()

    override fun <T> setAttribute(key: String, value: T) {
        attributes[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getAttribute(key: String): T? = attributes[key] as? T

    companion object {
        private fun generateSessionId(): String = java.util.UUID.randomUUID().toString()
    }
}