package po.auth.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import po.auth.AuthSessionManager
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import kotlin.coroutines.CoroutineContext


suspend fun session(identifyData: SessionIdentified): AuthorizedSession
    = AuthSessionManager.authenticator.authorize(identifyData)

suspend fun withSession(
    session: AuthorizedSession,
    block: suspend CoroutineScope.() -> Unit
) = withContext(session.sessionContext, block)


suspend fun CoroutineScope.currentSession(): AuthorizedSession?{
    return coroutineContext[AuthorizedSession]
}


//suspend fun withSession(identity: String, identifiedBy: IdentifiedBy,  block: suspend AuthorizedSession.() -> Unit){
//    val session = AuthSessionManager.getActiveSessions().firstOrNull{ it.sessionId == identity }.getOrThrow("Session for id: $identity not found",
//        ErrorCodes.SESSION_NOT_FOUND)
//    withContext(session) { session.block() }
//}

