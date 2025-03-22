package po.exposify.scope.session.interfaces

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Represents a user-specific coroutine session context, carrying authentication
 * and per-session data across suspend calls.
 */
interface UserSession :   CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = Key

    val userId: Long?
    val sessionId: String
    val createdAt: Long

    fun <T>setAttribute(key: String, value: T & Any)
    fun <T>getAttribute(key: String): T?

    companion object Key : CoroutineContext.Key<UserSession>
}