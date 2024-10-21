package po.api.rest_service.security


import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
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
import po.api.rest_service.exceptions.AuthErrorCodes
import po.api.rest_service.exceptions.AuthException
import java.util.concurrent.TimeUnit

class JWTService {

    private lateinit var audience: String
    private lateinit var issuer: String
    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey
    private lateinit var verifier: JWTVerifier

    private fun pemToBase64Str(pemKeyStr: String): String{
       return  pemKeyStr.replace("(?m)-----BEGIN.*?-----\\s*".toRegex(), "")
            .replace("(?m)-----END.*?-----\\s*".toRegex(), "")
            .replace("\\s+".toRegex(), "")
    }

    fun configure(config: JwtConfig):JWTService {

        audience = config.audience
        issuer = config.issuer

        try {
            if (config.jwkProvider != null) {
                val jwkProvider = JwkProviderBuilder(this.issuer)
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build()
                this.publicKey = jwkProvider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey as RSAPublicKey
            } else {
                val publicDecoded = Base64.getDecoder().decode(pemToBase64Str(config.publicKeyString))
                val publicKeySpec = X509EncodedKeySpec(publicDecoded)
                this.publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec) as RSAPublicKey
            }
        }catch (argE: IllegalArgumentException){
            throw AuthException(AuthErrorCodes.INVALID_KEY_FORMAT, "Error while parsing public key: ${argE.message}")
        }
        catch (e:Exception){
            throw AuthException(AuthErrorCodes.UNKNOWN_ERROR, "Error while parsing public key: ${e.message}")
        }

        try {
            val privateDecoded = Base64.getDecoder().decode(pemToBase64Str(config.privateKeyString))
            val privateKeySpec = PKCS8EncodedKeySpec(privateDecoded)
            this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec) as RSAPrivateKey
        }catch (argE: IllegalArgumentException){
            throw AuthException(AuthErrorCodes.INVALID_KEY_FORMAT, "Error while parsing private key: ${argE.message}")
        }
        catch (e:Exception){
            throw AuthException(AuthErrorCodes.UNKNOWN_ERROR, "Error while parsing private key: ${e.message}")
        }

        verifier = JWT.require(Algorithm.RSA256(publicKey, null))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()

        return this
    }

    fun getVerifier(): JWTVerifier {
        if(!::verifier.isInitialized) throw AuthException(AuthErrorCodes.CONFIGURATION_MISSING, "Verifier not initialized. Call configure() first")
        return verifier
    }

    fun generateToken(user: SecureUser): String? {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .withPayload(user.toPayload())
            .sign(Algorithm.RSA256(null, privateKey))
    }

    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val verifier = getVerifier()
            verifier.verify(token)
        } catch (e: Exception) {
            throw AuthException(AuthErrorCodes.INVALID_TOKEN, "Error while verifying token: ${e.message}")
        }
    }
}