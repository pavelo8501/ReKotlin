package po.auth.sessions.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import po.auth.AuthSessionManager

import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession


suspend fun <T: CoroutineScope>  T.authorizedSession(
    principal:  AuthorizedPrincipal,
    block : (suspend AuthorizedSession.() ->T)? = null
):AuthorizedSession{
    val default =  AuthSessionManager.createSession(principal)
    if(block != null){ default.block() }
    return default
}

suspend fun <T: CoroutineScope>  T.anonymousSession(
    principal: AuthorizedPrincipal? = null,
    block : (suspend AuthorizedSession.() ->T)? = null
):AuthorizedSession?{
    val default =  AuthSessionManager.createAnonymousSession(principal)
    if(block != null){
        default.block()
    }
    return default
}

