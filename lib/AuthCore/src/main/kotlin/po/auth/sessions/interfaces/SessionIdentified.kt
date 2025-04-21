package po.auth.sessions.interfaces

interface SessionIdentified {
    val sessionId: String
    val remoteAddress: String
}