package po.auth

import kotlinx.coroutines.withContext
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Utilities for creating, accessing, and executing within session context.
 */
object AuthSessionManager : ManagedSession {

    private var authenticator : UserAuthenticator? = null
    private val internalStore : ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()
    private val factory : SessionFactory = SessionFactory(this, internalStore)

    fun <T : Any> registerPrincipalBuilder(
        builder: () -> AuthorizedPrincipal
    ) {
        authenticator?.setBuilderFn(builder)
    }

    fun enableCredentialBasedAuthentication(
        validatorFn: (String, String) -> AuthenticationPrincipal?,
        principalBuilder: () -> AuthorizedPrincipal){
        authenticator = UserAuthenticator(validatorFn, principalBuilder, factory)
    }

    fun sessionManager(): ManagedSession{
        return  this
    }

    suspend fun <T> withSession(session: AuthorizedSession, block: suspend AuthorizedSession.() -> T): T =
        withContext(session) { session.block() }

//    suspend fun <T> withAnonymousSession(session: AnonymousSession, block: suspend AnonymousSession.() -> T): T =
//        withContext(session) { session.block() }

    override suspend fun getSessions(): List<EmmitableSession> = getActiveSessions()
    suspend fun getActiveSessions(): List<AuthorizedSession> = factory.activeSessions()


    override suspend fun getAnonymous():EmmitableSession? = getAnonymousSession() as EmmitableSession?
    suspend fun getAnonymousSession(): AuthorizedSession? =  factory.getAnonymousSession()


    fun createSession(principal : AuthenticationPrincipal): AuthorizedSession = factory.createSession(principal)

//    suspend fun createAnonymousSession(anonymousUser: AuthenticatedPrincipal?): AnonymousSession? {
//        return factory.createAnonymousSession(anonymousUser)
//    }

    override suspend fun getCurrentSession(): AuthorizedSession?{
        return  coroutineContext[PrincipalKey] as AuthorizedSession?
    }

    suspend fun <T> getOrCreateSession(
        principal: AuthorizedPrincipal,
        block: suspend AuthorizedSession.() -> T): T {
        return getCurrentSession()?.let { withSession(it, block) } ?: withSession(createSession(principal), block)
    }

    suspend fun <T> createSession(userName: String, password: String, block: suspend AuthorizedSession.() -> T?): T? {
         authenticator?.authenticateAndConstruct(userName, password)?.let {
             return withSession(it, block)
         }?: return  null
    }

    object PrincipalKey: CoroutineContext.Key<AuthorizedSession>

}