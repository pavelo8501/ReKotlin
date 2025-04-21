package po.auth

import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.extensions.getOrThrow
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.extensions.ifNull
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.lognotify.extensions.withLastTask
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

    fun <T : Any> registerPrincipalBuilder(
        builder: () -> AuthorizedPrincipal
    )
    {
        authenticator.setBuilderFn(builder)
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

    suspend fun getOrCreateSession(authData: AuthenticationData): AuthorizedSession{
        return authenticator.authenticate(authData)
    }

    override suspend fun getCurrentSession(): AuthorizedSession =
        withLastTask {handler->
           val fromContext =  coroutineContext[AuthorizedSession]
            fromContext.ifNull{
                handler.warn("CurrentSession is null. Creating anonymous")
                factory.createAnonymousSession(AuthenticationData("unknown" ,"localhost", emptyMap(),"",""), authenticator)
            }
        }.getOrThrow("Failed after all attempts", ErrorCodes.UNINITIALIZED)
}