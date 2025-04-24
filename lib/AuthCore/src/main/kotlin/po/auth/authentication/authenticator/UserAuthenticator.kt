package po.auth.authentication.authenticator

import at.favre.lib.crypto.bcrypt.BCrypt
import okio.Path
import po.auth.authentication.authenticator.interfaces.AuthenticationProvider
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.jwt.JWTService
import po.auth.extensions.getOrThrow
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession

class UserAuthenticator(
    private val factory : SessionFactory
): AuthenticationProvider {


    var keyBasePath : Path? = null

    private var _jwtService : JWTService? = null
    val jwtService: JWTService get() = _jwtService.getOrThrow("JWTService unavailable", ErrorCodes.UNINITIALIZED)

    private var lookupFn : (suspend (login: String)-> AuthenticationPrincipal?)? = null
    fun setAuthenticator(userLookupFn: suspend (login: String)-> AuthenticationPrincipal?){
        lookupFn = userLookupFn
    }

    fun generatePasswordHash(plainPassword : String): String{
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
    }

    suspend fun authenticate(login: String, password: String, anonymous: AuthorizedSession): AuthenticationPrincipal{
       return lookupFn?.let {principalValidatorFn->
           val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
           principalValidatorFn(login)?.let {
              if( BCrypt.verifyer().verify(password.toCharArray(), it.hashedPassword) == null){
                  throw AuthException("Password mismatch", ErrorCodes.AUTH_PASSWORD_MISMATCH )
              }
               factory.createAuthorizedSession(anonymous.sessionId, it)
               it
           }

        }?:throw AuthException("Authenticate function not set", ErrorCodes.CONFIGURATION_MISSING)
    }

    fun provideJwtService(service : JWTService){
        _jwtService = service
    }

    override suspend fun authorize(authData: SessionIdentified): AuthorizedSession {
        val existentSession = factory.activeSessions[authData.sessionId]
        return existentSession ?: factory.createAnonymousSession(authData, this)
    }

}