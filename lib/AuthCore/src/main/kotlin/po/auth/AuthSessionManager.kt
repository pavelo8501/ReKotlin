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

fun echo(ex: Exception, message: String? = null){
    println("Exception happened in AuthSessionManager: Exception:${ex.message.toString()}. $message")
}

/**
 * Utilities for creating, accessing, and executing within session context.
 */
object AuthSessionManager : ManagedSession {

    private var authenticator : UserAuthenticator? = null
    private val internalStore : ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()
    private val factory : SessionFactory = SessionFactory(this, internalStore)

    fun <T : Any> registerPrincipalBuilder(
        builder: () -> AuthorizedPrincipal
    )
    {
        authenticator?.setBuilderFn(builder)
    }

    private fun echo(ex: Exception, message: String? = null){
        println("Exception happened in AuthSessionManager: Exception:${ex.message.toString()}. $message")
    }

    fun enableCredentialBasedAuthentication(
        validatorFn: (String, String) -> AuthenticationPrincipal?,
        principalBuilder: () -> AuthorizedPrincipal){
        authenticator = UserAuthenticator(validatorFn, principalBuilder, factory)
    }

    suspend fun <T> withSession(session: AuthorizedSession, block: suspend AuthorizedSession.() -> T): T =
        withContext(session) { session.block() }

    override suspend fun getSessions(): List<EmmitableSession> = getActiveSessions()
    suspend fun getActiveSessions(): List<AuthorizedSession> = factory.activeSessions()


    override suspend fun getAnonymous():EmmitableSession {
      return getAnonymousSession() as EmmitableSession
    }
    suspend fun getAnonymousSession(): AuthorizedSession? {
        try {
            val asEmmitable = factory.getAnonymousSession()
            return asEmmitable
        }catch (ex: Exception){
            echo(ex)
            return null
        }
    }

    fun createSession(principal : AuthorizedPrincipal): AuthorizedSession = factory.createSession(principal)

    fun createAnonymousSession(anonymousUser: AuthenticationPrincipal? = null): AuthorizedSession {
        try {
            return factory.createAnonymousSession(anonymousUser)
        }catch (ex: Exception){
            echo(ex)
            throw ex
        }
    }

    override suspend fun getCurrentSession(): AuthorizedSession{
       try{
           val session =  coroutineContext[AuthorizedSessionKey]
           return session ?: createAnonymousSession(null)
       }catch (ex: Exception){
           echo(ex)
           throw ex
       }
    }

    suspend fun <T> getOrCreateSession(
        principal: AuthorizedPrincipal,
        block: suspend AuthorizedSession.() -> T): T
    {
        try{
            return withSession(getCurrentSession(), block) ?: withSession(createSession(principal), block)
        }catch (ex: Exception){
            po.auth.echo(ex)
            throw  ex
        }
    }

//    suspend fun <T> createSession(
//        userName: String, password: String,
//        block: suspend AuthorizedSession.() -> T?):T?
//    {
//         authenticator?.authenticateAndConstruct(userName, password)?.let {
//             return withSession(it, block)
//         }?: return  null
//    }

    object AuthorizedSessionKey: CoroutineContext.Key<AuthorizedSession>

}