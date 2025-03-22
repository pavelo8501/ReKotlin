package po.auth.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.auth.interfaces.UserSession
import po.auth.sessions.interfaces.SessionLifecycleCallback
import java.util.concurrent.ConcurrentHashMap
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

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO + this

    // Typed storages
    private val sessionStore = ConcurrentHashMap<String, Any?>()
    private val roundTripStore = ConcurrentHashMap<String, Any?>()
    private val externalStore = ConcurrentHashMap<String, Any?>()

    var lifecycleCallback: SessionLifecycleCallback? = null

    override fun <T> setAttribute(key: String, value: T) {
        sessionStore[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getAttribute(key: String): T? = sessionStore[key] as? T

    fun <T> setSessionAttr(name: String, value: T) {
        sessionStore[name] = value
    }

    fun <T> getSessionAttr(name: String): T? = sessionStore[name] as? T

    fun <T> setRoundTripAttr(name: String, value: T) {
        roundTripStore[name] = value
    }

    fun <T> getRoundTripAttr(name: String): T? = roundTripStore[name] as? T

    fun <T> setExternalRef(name: String, value: T) {
        externalStore[name] = value
    }

    fun <T> getExternalRef(name: String): T? = externalStore[name] as? T

    fun triggerProcessStart() {
        lifecycleCallback?.onProcessStart(this)
    }

    fun triggerProcessEnd() {
        roundTripStore.clear()
        lifecycleCallback?.onProcessEnd(this)
    }

    companion object {
        private fun generateSessionId(): String = java.util.UUID.randomUUID().toString()
    }
}