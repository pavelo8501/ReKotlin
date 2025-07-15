package po.auth.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import po.auth.AuthSessionManager
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import kotlin.coroutines.coroutineContext


fun session(identifyData: SessionIdentified): AuthorizedSession
    = AuthSessionManager.authenticator.authorize(identifyData)

suspend fun withSessionContext(
    session: AuthorizedSession,
    block: suspend CoroutineScope.() -> Unit
) = withContext(session.sessionScope().coroutineContext, block)

suspend fun withSessionContext(
    sessionIdentity: SessionIdentified,
    block: suspend CoroutineScope.() -> Unit
) {
    val session =  coroutineContext[AuthorizedSession]?: AuthSessionManager.getOrCreateSession(sessionIdentity)
    withContext(session.sessionScope().coroutineContext, block)
}


fun<R> withSession(
    session: AuthorizedSession,
    block: AuthorizedSession.() -> R
):R{
    return block.invoke(session)
}

fun <R> withSession(
    sessionIdentity : SessionIdentified,
    block:  AuthorizedSession.() -> R
):R {
   return block.invoke(AuthSessionManager.getOrCreateSessionSync(sessionIdentity))
}

suspend fun <R> withSessionSuspended(
    sessionIdentity : SessionIdentified,
    block: suspend AuthorizedSession.() -> R
):R {
   return coroutineContext[AuthorizedSession]?.let {
        block.invoke(it)
    }?:run {
       block.invoke(AuthSessionManager.getOrCreateSession(sessionIdentity))
   }
}


suspend fun CoroutineScope.currentSession(): AuthorizedSession?{
    return coroutineContext[AuthorizedSession]
}


//suspend fun withSession(identity: String, identifiedBy: IdentifiedBy,  block: suspend AuthorizedSession.() -> Unit){
//    val session = AuthSessionManager.getActiveSessions().firstOrNull{ it.sessionId == identity }.getOrThrow("Session for id: $identity not found",
//        ErrorCodes.SESSION_NOT_FOUND)
//    withContext(session) { session.block() }
//}

