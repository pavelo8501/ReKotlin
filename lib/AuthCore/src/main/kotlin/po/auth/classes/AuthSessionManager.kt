package po.auth.classes

import kotlinx.coroutines.withContext
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.interfaces.AuthenticatedPrincipal
import po.auth.interfaces.UserSession

import kotlin.coroutines.*

/**
 * Utilities for creating, accessing, and executing within session context.
 */
object AuthSessionManager {

    private var authenticator : UserAuthenticator? = null

    fun enableCredentialBasedAuthentication(validatorFn: (String, String) -> AuthenticatedPrincipal?){
        authenticator = UserAuthenticator(validatorFn)
    }

    suspend fun <T> withSession(session: UserSession, block: suspend UserSession.() -> T): T =
        withContext(session) { session.block() }

    suspend fun getCurrentSession(): UserSession? = coroutineContext[UserSession]

    suspend fun requireCurrentSession(): UserSession =
        getCurrentSession() ?: error("No active session in current coroutine context")

    suspend fun <T> getOrCreateSession(userId: Long, create: () -> UserSession, block: suspend UserSession.() -> T): T {
        return getCurrentSession()?.let { withSession(it, block) } ?: withSession(create(), block)
    }

    suspend fun <T> createSession(userName: String, password: String, create: () -> UserSession, block: suspend UserSession.() -> T): T{
        authenticator?.let {
          val authenticationResult =  it.authenticate(userName, password)
          if(authenticationResult == false){
              throw Exception("Authentication for given credentials failed. Username : $userName")
          }else{
             return  withSession(create(), block)
          }
        }?: run {
            throw Exception("Session creation requested but no Authentication method enabled")
        }
    }

    suspend fun <T> createAnonymousSession(anonymousUser: AuthenticatedPrincipal, create: (AuthenticatedPrincipal) -> UserSession, block: suspend UserSession.() -> T): T{
        return getCurrentSession()?.let { withSession(it, block) } ?: withSession(create(anonymousUser), block)
    }

}