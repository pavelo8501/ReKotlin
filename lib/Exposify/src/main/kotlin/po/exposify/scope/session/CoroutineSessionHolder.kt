package po.exposify.scope.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import po.exposify.scope.session.interfaces.UserSession

import kotlin.coroutines.coroutineContext


/**
 * Coroutine-local session utilities.
 */
object CoroutineSessionHolder {


    suspend fun <T> withSession(
        session: UserSession,
        block: suspend CoroutineScope.() -> T
    ): T = withContext(session, block)


    suspend fun getCurrentContext(): UserSession? {
        return coroutineContext[UserSession]
    }

    suspend fun requireCurrentContext(): UserSession {
        return getCurrentContext() ?: error("No session available in current coroutine context")
    }

}
