package po.auth.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import po.auth.AuthSessionManager
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


suspend fun session(identifyData: SessionIdentified): AuthorizedSession
    = AuthSessionManager.authenticator.authorize(identifyData)

suspend fun withSessionContext(
    session: AuthorizedSession,
    block: suspend CoroutineScope.() -> Unit
) = withContext(session.sessionContext, block)


fun<R> withSessionSync(
    session: AuthorizedSession,
    block: AuthorizedSession.() -> R
):R{
    return block.invoke(session)
}

fun <R> withSessionSync(
    sessionIdentity : SessionIdentified,
    block:  AuthorizedSession.() -> R
):R {
   return block.invoke(AuthSessionManager.getOrCreateSessionSync(sessionIdentity))
}

suspend fun <R> withSession(
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

