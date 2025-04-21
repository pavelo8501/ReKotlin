package po.restwraptor.models.configuration

import java.security.PrivateKey
import java.security.PublicKey

data class AuthConfig(
    val authRoutePrefix : String = "auth",
    val enableSecurity : Boolean = true,
    val defaultSecurityRouts : Boolean = true,
    val jwtServiceName : String = "auth-jwt"
) {

    internal var privateKey: PrivateKey? = null
    internal var publicKey: PublicKey? = null

    var useWellKnownHost: Boolean = false
    var wellKnownPath: String? = null

    fun setAuthKeys(privateKey: PrivateKey, publicKey: PublicKey, ) {
        this.privateKey = privateKey
        this.publicKey = publicKey
        this.wellKnownPath = null

    }

    fun setWellKnown(path: String) {
        TODO("Not yet implemented")
    }

}