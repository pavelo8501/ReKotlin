package po.auth

import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext


/**
 * Utilities for creating, accessing, and executing within session context.
 */
object AuthSessionManager : ManagedSession, TasksManaged {

    override val contextName: String = "AuthSessionManager"

    override val identity: CTXIdentity<AuthSessionManager> = asIdentity()

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

    override suspend fun getSession(sessionId: String?):AuthorizedSession?{
        if(sessionId == null){
            return null
        }
        return  factory.sessionLookUp(sessionId)
    }

    override suspend fun getAnonymousSessions(): List<AuthorizedSession> = factory.listAnonymous()

    suspend fun getOrCreateSession(authData: SessionIdentified): AuthorizedSession{
        return authenticator.authorize(authData)
    }

    fun getOrCreateSessionSync(authData: SessionIdentified): AuthorizedSession{
        return authenticator.authorizeSync(authData)
    }

    override suspend fun getCurrentSession(): AuthorizedSession{
        val session  = coroutineContext[AuthorizedSession]?:
               factory.createAnonymousSession(AuthenticationData("Undefined", "localhost", emptyMap(),"",""), authenticator)
        return session
    }
}