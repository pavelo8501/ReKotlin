package po.auth.sessions.interfaces

import po.auth.models.DefaultSessionContext


interface SessionLifecycleCallback {
    fun onProcessStart(session: DefaultSessionContext)
    fun onProcessEnd(session: DefaultSessionContext)
}