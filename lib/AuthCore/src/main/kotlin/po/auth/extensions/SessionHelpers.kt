package po.auth.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import po.auth.AuthSessionManager
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import kotlin.coroutines.coroutineContext


fun session(identifyData: SessionIdentified): AuthorizedSession =
    AuthSessionManager.authenticator.authorize(identifyData)

suspend fun withSessionContext(
    session: AuthorizedSession,
    block: suspend CoroutineScope.() -> Unit
) = withContext(session.coroutineContext, block)

suspend fun withSessionContext(
    sessionIdentity: SessionIdentified,
    block: suspend CoroutineScope.() -> Unit
) {
    val session =  currentCoroutineContext()[AuthorizedSession]?: AuthSessionManager.getOrCreateSession(sessionIdentity)
    withContext(session.coroutineContext, block)
}

inline fun <R> AuthorizedSession.runWithSession(
    block: AuthorizedSession.() -> R
):R{
   return block()
}



suspend fun CoroutineScope.currentSession(): AuthorizedSession?{
   return currentCoroutineContext()[AuthorizedSession]
}
