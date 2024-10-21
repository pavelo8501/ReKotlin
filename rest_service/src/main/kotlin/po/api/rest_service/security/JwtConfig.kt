package po.api.rest_service.security


import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

data class JwtConfig(
    val realm: String = "Access to secure API",
    val audience: String = "jwt-audience",
    val issuer: String = "http://0.0.0.0:8080/"
) {

    val secret: String = "secret"

    lateinit var privateKeyString: String
    lateinit var publicKeyString: String
    var jwkProvider : JwkProvider? = null


    fun setKeys(publicKeyString: String,privateKeyString: String) {
        this.publicKeyString = publicKeyString
        this.privateKeyString = privateKeyString
    }


    fun setKeys(jwkProvider: JwkProvider, privateKeyString: String) {
        this.jwkProvider = jwkProvider
        this.privateKeyString = privateKeyString
    }


//    val algorithm: Algorithm = Algorithm.HMAC256(secret)
//    val verifier: JWTVerifier = JWT.require(algorithm)
//        .withAudience(audience)
//        .withIssuer(issuer)
//        .build()
}