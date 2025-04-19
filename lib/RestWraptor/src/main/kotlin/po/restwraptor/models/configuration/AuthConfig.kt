package po.restwraptor.models.configuration

import java.security.PrivateKey
import java.security.PublicKey

data class AuthConfig(
    val authRoutePrefix : String = "auth",
    val enableSecurity : Boolean = true,
    val defaultSecurityRouts : Boolean = true,
    val jwtServiceName : String = "auth-jwt"
) {

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