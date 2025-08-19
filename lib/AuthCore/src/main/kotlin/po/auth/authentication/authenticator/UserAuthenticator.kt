package po.auth.authentication.authenticator

import at.favre.lib.crypto.bcrypt.BCrypt
import okio.Path

import po.auth.authentication.authenticator.interfaces.AuthenticationProvider
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.exceptions.AuthException
import po.auth.authentication.jwt.JWTService
import po.auth.exceptions.AuthErrorCode
import po.auth.exceptions.authException
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.misc.types.getOrThrow

typealias AuthFunction = (suspend (login: String)-> AuthenticationPrincipal?)

class UserAuthenticator(
    private val factory : SessionFactory
): AuthenticationProvider {

    var keyBasePath : Path? = null

    private var _jwtService : JWTService? = null
    val jwtService: JWTService get() = _jwtService.getOrThrow<JWTService>(JWTService::class, this){payload ->
        authException(payload.setCode(AuthErrorCode.UNINITIALIZED))
    }

    private var lookupFn : (suspend (login: String)-> AuthenticationPrincipal?)? = null
    fun setAuthenticator(userLookupFn: suspend (login: String)-> AuthenticationPrincipal?){
        lookupFn = userLookupFn
    }

    fun generatePasswordHash(plainPassword : String): String{
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
    }

    suspend fun authenticate(login: String, password: String, anonymous: AuthorizedSession): AuthenticationPrincipal{
        val principalLookupFn = lookupFn.getOrThrow<AuthFunction>(Function::class, this){ payload->
            authException("Authenticate function not set", AuthErrorCode.CONFIGURATION_MISSING)
        }
        val principal =  principalLookupFn.invoke(login)
        if(principal == null){
           throw authException("Wrong login", AuthErrorCode.INVALID_CREDENTIALS)
        }
        if( BCrypt.verifyer().verify(password.toCharArray(), principal.hashedPassword) == null){
            throw authException("Password mismatch", AuthErrorCode.PASSWORD_MISMATCH, null)
        }
        anonymous.providePrincipal(principal)
        return principal
    }

    fun provideJwtService(service : JWTService){
        _jwtService = service
    }

    override fun authorize(authData: SessionIdentified): AuthorizedSession {


        val existentSession = factory.sessionLookUp(authData)
        return existentSession ?: factory.createAnonymousSession(authData)
    }

    fun authorizeSync(authData: SessionIdentified): AuthorizedSession {
        val existentSession = factory.sessionLookUp(authData)
        return existentSession ?: factory.createAnonymousSession(authData)
    }

}