package po.auth.sessions.interfaces

import kotlinx.coroutines.CoroutineScope
import po.auth.sessions.models.AuthorizedPrincipal

interface  EmmitableSession  :  SessionLifecycleCallback {
    val principal : AuthorizedPrincipal
    val sessionId: String
    val scope : CoroutineScope
}
