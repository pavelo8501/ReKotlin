package po.auth.authentication.authenticator

import at.favre.lib.crypto.bcrypt.BCrypt
import okio.Path
import po.auth.authentication.authenticator.interfaces.AuthenticationProvider
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.jwt.JWTService
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.misc.exceptions.HandlerType
import po.misc.types.getOrThrow



typealias AuthFunction = (suspend (login: String)-> AuthenticationPrincipal?)

class UserAuthenticator(
    private val factory : SessionFactory
): AuthenticationProvider {


    var keyBasePath : Path? = null

    private var _jwtService : JWTService? = null
    val jwtService: JWTService get() = _jwtService.getOrThrow<JWTService, AuthException>()

    private var lookupFn : (suspend (login: String)-> AuthenticationPrincipal?)? = null
    fun setAuthenticator(userLookupFn: suspend (login: String)-> AuthenticationPrincipal?){
        lookupFn = userLookupFn
    }

    fun generatePasswordHash(plainPassword : String): String{
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
    }

    suspend fun authenticate(login: String, password: String, anonymous: AuthorizedSession): AuthenticationPrincipal{
        val principalLookupFn = lookupFn.getOrThrow<AuthFunction, AuthException>("Authenticate function not set", ErrorCodes.CONFIGURATION_MISSING.value)
        val principal =  principalLookupFn.invoke(login)
        if(principal == null){
           throw AuthException("Wrong login", ErrorCodes.INVALID_CREDENTIALS).setHandler(HandlerType.SKIP_SELF)
        }
        if( BCrypt.verifyer().verify(password.toCharArray(), principal.hashedPassword) == null){
            throw AuthException("Password mismatch", ErrorCodes.PASSWORD_MISMATCH).setHandler(HandlerType.SKIP_SELF)
        }
        anonymous.providePrincipal(principal)
        return principal
    }

    fun provideJwtService(service : JWTService){
        _jwtService = service
    }

    override suspend fun authorize(authData: SessionIdentified): AuthorizedSession {
        val existentSession = factory.sessionLookUp(authData.sessionID)
        return existentSession ?: factory.createAnonymousSession(authData, this)
    }

    fun authorizeSync(authData: SessionIdentified): AuthorizedSession {
        val existentSession = factory.sessionLookUp(authData.sessionID)
        return existentSession ?: factory.createAnonymousSession(authData, this)
    }

}