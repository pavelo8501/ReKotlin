package po.restwraptor.models.configuration

import kotlinx.io.IOException
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.request.LoginRequest
import java.io.File

class AuthenticationConfig {

    var defaultSecurityRouts : Boolean = true

    val security: Boolean
        get(){
            if(publicKeyString!!.isNotEmpty() && privateKeyString!!.isNotEmpty()){
                return true
            }
            if(wellKnownPath!= null){
                return true
            }
            return false
        }
    var jwtServiceName = "auth-jwt"
    var credentialsValidatorFn : ((LoginRequest)-> SecuredUserInterface?)? = null
    internal var privateKeyString: String? = null
    internal var publicKeyString: String? = null
    var useWellKnownHost: Boolean = false
    var wellKnownPath: String? = null

    fun setAuthKeys(publicKey: String, privateKey: String) {
        this.publicKeyString = publicKey
        this.privateKeyString = privateKey
        this.wellKnownPath = null

    }

    fun setWellKnown(path: String) {
        TODO("Not yet implemented")
        this.wellKnownPath = path
        this.publicKeyString = null
        this.privateKeyString = null
    }




}