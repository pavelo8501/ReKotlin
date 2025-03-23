package po.auth.sessions.interfaces


import kotlinx.coroutines.CoroutineScope
import po.auth.sessions.models.AuthorizedPrincipal
import kotlin.coroutines.CoroutineContext

interface  EmmitableSession  :  SessionLifecycleCallback {
    val principal : AuthorizedPrincipal
    val sessionId: String
}
