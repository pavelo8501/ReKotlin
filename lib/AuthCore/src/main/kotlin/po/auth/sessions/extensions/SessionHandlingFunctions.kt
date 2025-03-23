package po.auth.sessions.extensions

import kotlinx.coroutines.CoroutineScope
import po.auth.AuthSessionManager
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.models.DefaultSession


suspend fun <T> withSession(session: DefaultSession, block: suspend DefaultSession.() -> T): T =
    AuthSessionManager.withSession(session, block)


suspend fun <T> withSession(session: AnonymousSession, block: suspend AnonymousSession.() -> T): T =
    AuthSessionManager.withAnonymousSession(session, block)

suspend fun CoroutineScope.getCurrentContext():  EmmitableSession? {
   return  AuthSessionManager.getCurrentSession()

}

