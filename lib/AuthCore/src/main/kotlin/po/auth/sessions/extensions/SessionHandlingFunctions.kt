package po.auth.sessions.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import po.auth.AuthSessionManager
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession


suspend fun <T> withSession(session: AuthorizedSession, block: suspend AuthorizedSession.() -> T): T =
    AuthSessionManager.withSession(session, block)

suspend fun <T> anonymousSession(
    principal: AuthorizedPrincipal? = null,
    block : (suspend AuthorizedSession.() ->T)? = null
):AuthorizedSession?{
    val default =  AuthSessionManager.createAnonymousSession(principal)
    if(block != null){
        default.block()
    }
    return default
}


suspend fun <R>  R.authorizedSession(
    principal:  AuthorizedPrincipal,
    block : (suspend AuthorizedSession.() ->R?)? = null
):AuthorizedSession{
    val default =  AuthSessionManager.createSession(principal)
    if(block != null){
        default.block()
    }
    return default
}


suspend fun CoroutineScope.getCurrentContext():  EmmitableSession? {
   return  AuthSessionManager.getCurrentSession()
}


suspend fun AuthorizedSession.snapshot(label: String = "SESSION SNAPSHOT") {
    val YELLOW = "\u001B[33m"
    val RESET = "\u001B[0m"

    println("""
        $YELLOW
        --- [$label] ---
        🔑 sessionId     = $sessionId
        🧑 principal      = ${principal.login} (${principal.userId})
        🧠 sessionType    = $sessionType
        🗂️  internalStore = ${internalStore.keys.joinToString(", ")}
        🎯 registeredHandlers = ${
          getAttributeKeys().joinToString(", ")
    }
        🧵 coroutineScope = ${scope.coroutineContext[Job]?.let { if (it.isActive) "ACTIVE" else "INACTIVE" }}
        ---------------------
        $RESET
    """.trimIndent())
}

