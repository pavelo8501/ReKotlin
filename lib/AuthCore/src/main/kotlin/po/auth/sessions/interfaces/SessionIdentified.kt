package po.auth.sessions.interfaces

import po.auth.sessions.enumerators.SessionType

interface SessionIdentified {
    val sessionId: String
    val remoteAddress: String
}