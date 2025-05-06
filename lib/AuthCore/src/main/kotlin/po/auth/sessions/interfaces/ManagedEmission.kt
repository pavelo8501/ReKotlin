package po.auth.sessions.interfaces

import po.auth.sessions.models.AuthorizedSession


interface ManagedSession {
    suspend fun getCurrentSession(): AuthorizedSession
    suspend fun getSession(sessionId: String?):AuthorizedSession?
    suspend fun getAnonymousSessions():List<AuthorizedSession>
}

