package po.auth.authentication.authenticator

import po.auth.authentication.authenticator.interfaces.AuthenticationProvider
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.extensions.getOrThrow
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.authentication.jwt.JWTService
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession
import kotlin.invoke

class UserAuthenticator(
    private val factory : SessionFactory
): AuthenticationProvider {

    private var validatorFn: ((userName: String, password: String)-> AuthenticationPrincipal)? = null
    private var principalBuilder: (() ->  AuthorizedPrincipal)? = null

    private var _jwtService : JWTService? = null
    val jwtService: JWTService get() = _jwtService.getOrThrow("JWTService unavailable", ErrorCodes.UNINITIALIZED)


    fun builder (principal : AuthenticationPrincipal) : AuthorizedSession?{
      // return factory.createSession(principalBuilder())
        return  null
    }

    var authFn : (suspend (login: String, password: String)-> AuthorizedPrincipal?)? = null
    fun setAuthenticator(callback: suspend (login: String, password: String)-> AuthorizedPrincipal?){
        authFn = callback
    }

    suspend fun authenticate(login: String, password: String): AuthorizedPrincipal{
        return  authFn?.invoke(login, password) ?:run {
            throw AuthException("Authenticate function not set", ErrorCodes.CONFIGURATION_MISSING)
        }
    }


    fun setBuilderFn(fn: () ->  AuthorizedPrincipal){
        principalBuilder =  fn
    }

    fun provideJwtService(service : JWTService){
        _jwtService = service
    }

    override suspend fun authenticate(authData: AuthenticationData): AuthorizedSession {
        val existentSession = factory.activeSessions[authData.sessionId]
        if(existentSession != null){
            val a = 10
        }
        return existentSession ?: factory.createAnonymousSession(authData, this)
    }

}