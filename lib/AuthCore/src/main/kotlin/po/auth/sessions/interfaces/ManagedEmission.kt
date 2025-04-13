package po.auth.sessions.interfaces



interface ManagedSession {
    suspend fun getCurrentSession(): EmmitableSession?
    suspend fun getSessions(): List<EmmitableSession>
    suspend fun getAnonymous(): EmmitableSession
}

