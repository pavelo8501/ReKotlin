package po.auth.sessions.extensions

import kotlinx.coroutines.CoroutineScope
import po.auth.AuthSessionManager
import po.auth.authentication.interfaces.AuthenticatedPrincipal
import po.auth.sessions.interfaces.AuthorizedPrincipal
import po.auth.sessions.models.DefaultSession


suspend fun <T: CoroutineScope>  T.createAuthorizedContext(
    principal: AuthorizedPrincipal,
    block : (suspend DefaultSession.() ->T)? = null
):DefaultSession{
    val default =  AuthSessionManager.createSession(principal)
    if(block != null){ default.block() }
    return default
}

suspend fun <T: CoroutineScope>  T.createAnonymousContext(
    principal: AuthenticatedPrincipal?,
    block : (suspend AnonymousSession.() ->T)? = null
):AnonymousSession?{
    val default =  AuthSessionManager.createAnonymousSession(principal)
    if(default != null){
        if(block != null){ default.block() }
        return default
    }else{ return null }
}

