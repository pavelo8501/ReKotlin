package po.auth.sessions.interfaces

import kotlinx.coroutines.CoroutineScope
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.models.AuthorizedPrincipal

interface  EmmitableSession  :  SessionLifecycleCallback {
    val sessionId: String
    val sessionType: SessionType
    val scope : CoroutineScope
}
