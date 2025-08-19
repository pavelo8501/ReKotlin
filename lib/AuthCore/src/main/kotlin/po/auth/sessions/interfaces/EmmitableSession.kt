package po.auth.sessions.interfaces

import kotlinx.coroutines.CoroutineScope
import po.auth.sessions.enumerators.SessionType
import kotlin.coroutines.CoroutineContext

interface  EmmitableSession  :  SessionLifecycleCallback {
    val sessionID: String
    val sessionType: SessionType
    val scope: CoroutineScope
    val sessionContext: CoroutineContext
}
