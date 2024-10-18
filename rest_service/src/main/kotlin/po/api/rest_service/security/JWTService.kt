package po.api.rest_service.security


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Date
import kotlinx.serialization.json.Json

import po.api.rest_service.common.SecureUser

object JWTService {

    private lateinit var audience: String
    private lateinit var issuer: String
    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey


    fun configure(privateKeyString: String, publicKeyString: String, audienceParam: String, issuerParam: String) {
        audience = audienceParam
        issuer = issuerParam

        // Decode and initialize RSA keys
        val privateDecoded = Base64.getDecoder().decode(privateKeyString)
        val privateKeySpec = PKCS8EncodedKeySpec(privateDecoded)
        privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec) as RSAPrivateKey

        val publicDecoded = Base64.getDecoder().decode(publicKeyString)
        val publicKeySpec = X509EncodedKeySpec(publicDecoded)
        publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec) as RSAPublicKey
    }

    fun configure(config: JwtConfig):JWTService {
        audience = config.audience
        issuer = config.issuer

        // Decode and initialize RSA keys
        val privateDecoded = Base64.getDecoder().decode(config.privateKeyString)
        val privateKeySpec = PKCS8EncodedKeySpec(privateDecoded)
        privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec) as RSAPrivateKey

        val publicDecoded = Base64.getDecoder().decode(config.publicKeyString)
        val publicKeySpec = X509EncodedKeySpec(publicDecoded)
        publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec) as RSAPublicKey
        return this
    }

    fun generateToken(user: SecureUser, jsonEncodedPayload: String): String? {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .withPayload(jsonEncodedPayload)
            .sign(Algorithm.RSA256(publicKey, privateKey))
    }

    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val verifier = JWT.require(Algorithm.RSA256(publicKey, privateKey))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()

            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }

}