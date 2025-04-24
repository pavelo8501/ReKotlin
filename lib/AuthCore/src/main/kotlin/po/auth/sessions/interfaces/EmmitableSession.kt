package po.auth.sessions.interfaces

import kotlinx.coroutines.CoroutineScope
import po.auth.sessions.enumerators.SessionType
import kotlin.coroutines.CoroutineContext

interface  EmmitableSession  :  SessionLifecycleCallback {
    val sessionId: String
    val sessionType: SessionType

    val sessionContext: CoroutineContext
    fun sessionScope(): CoroutineScope
}
