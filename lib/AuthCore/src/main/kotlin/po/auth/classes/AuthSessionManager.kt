package po.auth.classes

import kotlinx.coroutines.withContext
import po.auth.interfaces.UserSession

import kotlin.coroutines.*

/**
 * Utilities for creating, accessing, and executing within session context.
 */
object AuthSessionManager {
    suspend fun <T> withSession(session: UserSession, block: suspend UserSession.() -> T): T =
        withContext(session) { session.block() }

    suspend fun getCurrentSession(): UserSession? = coroutineContext[UserSession]

    suspend fun requireCurrentSession(): UserSession =
        getCurrentSession() ?: error("No active session in current coroutine context")

    suspend fun <T> getOrCreateSession(userId: Long, create: () -> UserSession, block: suspend UserSession.() -> T): T {
        return getCurrentSession()?.let { withSession(it, block) } ?: withSession(create(), block)
    }
}