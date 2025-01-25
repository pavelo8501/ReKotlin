package po.restwraptor.models.security

import com.auth0.jwk.JwkProvider

data class JwtConfig(
    val realm: String = "Access to secure API",
    val audience: String = "jwt-audience",
    val issuer: String = "http://0.0.0.0:8080/",
    val secret: String = "secret",
    var privateKeyString: String? = null,
    var publicKeyString: String? = null,
    var jwkProvider: JwkProvider? = null
) {
    fun setKeys(publicKeyString: String, privateKeyString: String) {
        this.publicKeyString = publicKeyString
        this.privateKeyString = privateKeyString
    }

    fun setJwkProvider(jwkProvider: JwkProvider, privateKeyString: String) {
        this.jwkProvider = jwkProvider
        this.privateKeyString = privateKeyString
    }
}