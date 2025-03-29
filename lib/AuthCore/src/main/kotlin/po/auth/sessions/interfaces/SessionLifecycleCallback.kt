package po.auth.sessions.interfaces


interface SessionLifecycleCallback {
    fun onProcessStart(session: EmmitableSession)
    fun onProcessEnd(session: EmmitableSession)
}