package po.auth

import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext


/**
 * Utilities for creating, accessing, and executing within session context.
 */
object AuthSessionManager : ManagedSession, TasksManaged {

    private val internalStore : ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()
    private val factory : SessionFactory = SessionFactory(this, internalStore)
    var authenticator : UserAuthenticator = UserAuthenticator(factory)
        private set

    fun registerAuthenticator(lookupFn: suspend (login: String)-> AuthenticationPrincipal?) {
        authenticator.setAuthenticator(lookupFn)
    }

    fun initJwtService(config: JwtConfig):JWTService{
        authenticator.provideJwtService(JWTService(config))
        return authenticator.jwtService
    }

    override suspend fun getSessions(): List<EmmitableSession> = getActiveSessions()
    suspend fun getActiveSessions(): List<AuthorizedSession> = factory.activeSessions.values.toList()

    override suspend fun getAnonymousSessions():List<EmmitableSession> = getActiveAnonymousSessions()
    suspend fun getActiveAnonymousSessions(): List<AuthorizedSession>
        = factory.activeSessions.values.filter { it.sessionType == SessionType.ANONYMOUS }

    suspend fun getOrCreateSession(authData: SessionIdentified): AuthorizedSession{
        return authenticator.authorize(authData)
    }

    override suspend fun getCurrentSession(): AuthorizedSession{
        val session  = coroutineContext[AuthorizedSession]?:
               factory.createAnonymousSession(AuthenticationData("unknown" ,"localhost", emptyMap(),"",""), authenticator)
        return session
    }
}