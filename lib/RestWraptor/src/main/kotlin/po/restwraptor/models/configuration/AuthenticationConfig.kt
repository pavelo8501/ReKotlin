package po.restwraptor.models.configuration

import kotlinx.io.IOException
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.request.LoginRequest
import java.io.File
import java.security.PrivateKey
import java.security.PublicKey

data class AuthenticationConfig(
    var baseAuthRoute : String = "/auth",
    var enableSecurity : Boolean = true,
    var defaultSecurityRouts : Boolean = true
) {

    val security: Boolean
        get(){
            if(publicKeyString != null && privateKeyString != null){
                return true
            }
            if(wellKnownPath!= null){
                return true
            }
            return false
        }
    var jwtServiceName = "auth-jwt"
    var credentialsValidatorFn : ((LoginRequest)-> SecuredUserInterface?)? = null
    internal var privateKeyString: PrivateKey? = null
    internal var publicKeyString: PublicKey? = null
    var useWellKnownHost: Boolean = false
    var wellKnownPath: String? = null

    fun setAuthKeys(privateKey: PrivateKey, publicKey: PublicKey, ) {
        this.publicKeyString = publicKey
        this.privateKeyString = privateKey
        this.wellKnownPath = null

    }

    fun setWellKnown(path: String) {
        TODO("Not yet implemented")
    }

}