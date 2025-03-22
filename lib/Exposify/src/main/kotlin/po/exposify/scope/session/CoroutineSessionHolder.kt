package po.exposify.scope.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import po.exposify.scope.session.interfaces.UserSession
import kotlin.coroutines.CoroutineContext

import kotlin.coroutines.coroutineContext



inline suspend fun <T> sessionScope(userId: Long, noinline block: suspend DefaultSessionContext.() -> T): T =
    CoroutineSessionHolder.getContextOrCreate(userId, block)


suspend fun CoroutineScope.requireCurrentContext(): UserSession {
    return getCurrentContext() ?: error("No session available in current coroutine context")
}

suspend fun CoroutineScope.getCurrentContext(): UserSession? {
    return coroutineContext[UserSession]
}

/**
 * Coroutine-local session utilities.
 */
object CoroutineSessionHolder {

    suspend fun <T> withSession(
        session: DefaultSessionContext,
        block: suspend DefaultSessionContext.() -> T
    ): T = withContext(session) {
        session.block()
    }


    suspend fun getCurrentContext(user : Long ): DefaultSessionContext? {
        @Suppress("UNCHECKED_CAST")
        return coroutineContext[UserSession] as DefaultSessionContext
    }

    suspend fun <T>requireCurrentContext(user : T): UserSession {
        return getCurrentContext(1) ?: error("No session available in current coroutine context")
    }

    suspend fun createSessionContext(userId: Long): DefaultSessionContext {
       val newContext =  DefaultSessionContext(userId)
        return  newContext
    }

    suspend fun <T>getContextOrCreate(userId: Long, context: suspend DefaultSessionContext.() -> T):T {
        getCurrentContext(userId)?.let {
            return withSession(it, context)
        }?:run{
            return withSession(createSessionContext(userId), context)
        }
    }

}
