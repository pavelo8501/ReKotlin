package po.auth

import kotlinx.coroutines.currentCoroutineContext
import po.auth.AuthSessionManager.activeSessions
import po.auth.AuthSessionManager.factory
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.models.RoundTripData
import po.auth.models.SessionOrigin
import po.auth.models.SessionStoreKey
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.auth.sessions.models.ScopedSession
import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import java.util.concurrent.ConcurrentHashMap


/**
 * Utilities for creating, accessing, and executing within session context.
 */
object AuthSessionManager : ManagedSession, TasksManaged {

    override val contextName: String = "AuthSessionManager"

    override val identity: CTXIdentity<AuthSessionManager> = asIdentity()

    private val internalStore : ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()
    internal val factory : SessionFactory = SessionFactory(this, internalStore)

    var activeSessions: MutableMap<SessionStoreKey, AuthorizedSession> = mutableMapOf()

    var authenticator : UserAuthenticator = UserAuthenticator(factory)
        private set

    val activescopedSessions = mutableListOf<ScopedSession>()

    fun registerScopedSession(session: ScopedSession):ScopedSession {
        activescopedSessions.add(session)
        return session
    }

    fun registerAuthenticator(lookupFn: suspend (login: String)-> AuthenticationPrincipal?) {
        authenticator.setAuthenticator(lookupFn)
    }

    fun initJwtService(config: JwtConfig):JWTService{
        authenticator.provideJwtService(JWTService(config))
        return authenticator.jwtService
    }

    override suspend fun getSession(authData: SessionIdentified):AuthorizedSession?{

        return  factory.sessionLookUp(authData)
    }

    override suspend fun getAnonymousSessions(): List<AuthorizedSession> = factory.listAnonymous()

    fun getOrCreateSession(authData: SessionIdentified): AuthorizedSession {
        return authenticator.authorize(authData)
    }


    fun getOrCreateSessionSync(authData: SessionIdentified): AuthorizedSession{
        return authenticator.authorizeSync(authData)
    }

    override suspend fun getCurrentSession(): AuthorizedSession{
        val session  = currentCoroutineContext()[AuthorizedSession]?:
               factory.createAnonymousSession(AuthenticationData("Undefined", "localhost", emptyMap(),"",""))
        return session
    }
}

fun <K: SessionIdentified> getOrNewSession(key: K): AuthorizedSession {

   with(AuthSessionManager){
     val sessionKey: SessionStoreKey = SessionStoreKey.createFrom(key)
     val existent = activeSessions[sessionKey]
      return if (existent != null){
           existent.roundTripInfo.lastOrNull()?.let {
               existent.roundTripInfo.add(RoundTripData(it.count, SessionOrigin.Persisted))
           }
          existent
       }else{
          val newSession =  factory.createAnonymousSession(key)
          val sessionKey: SessionStoreKey = SessionStoreKey.createFrom(key)
          activeSessions[sessionKey] = newSession
          newSession
       }
    }
}