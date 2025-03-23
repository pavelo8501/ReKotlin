package po.auth.sessions.keys

import po.auth.sessions.interfaces.AuthorizedPrincipal
import po.auth.sessions.interfaces.EmmitableSession
import kotlin.coroutines.CoroutineContext


object AuthorizedPrincipalKey : CoroutineContext.Key<AuthorizedPrincipal>
object SessionKey : CoroutineContext.Key<EmmitableSession>