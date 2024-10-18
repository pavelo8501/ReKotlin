package po.api.rest_service.security


import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

data class JwtConfig(
    val realm: String,
    val audience: String,
    val issuer: String,
    val secret: String,
    val privateKeyString: String,
    val publicKeyString: String
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}