package po.exposify.scope.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.exposify.scope.session.interfaces.UserSession
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


/**
 * Default in-memory implementation of SessionContext.
 */
class DefaultSessionContext(
    override val userId: Long?,
    override val sessionId: String = generateSessionId(),
    override val createdAt: Long = System.currentTimeMillis()
) : CoroutineScope,  UserSession {
    private val attributes = mutableMapOf<String, Any>()

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO + this

    override fun <T> setAttribute(key: String, value: T & Any){
         attributes.put(key, value)
    }


    override fun <T> getAttribute(key: String): T?  {

        val value = attributes[key] ?: return null
        return try {
            value as T
        } catch (ex: Exception) {
            println("⚠️ Attribute type mismatch: key=$key, actual=${value::class}")
            null
        }
    }

    companion object {
        private fun generateSessionId(): String = java.util.UUID.randomUUID().toString()
    }
}