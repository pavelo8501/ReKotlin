package po.api.rest_service.security

import com.auth0.jwk.JwkProvider
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
import po.api.rest_service.common.SecureUserContext
import po.api.rest_service.exceptions.AuthErrorCodes
import po.api.rest_service.exceptions.AuthException

data class JwtConfig(
    val realm: String = "Access to secure API",
    val audience: String = "jwt-audience",
    val issuer: String = "http://0.0.0.0:8080/"
) {

    val secret: String = "secret"

    var privateKeyString: String? = null
    var publicKeyString: String? = null
    var jwkProvider : JwkProvider? = null

    fun setKeys(publicKeyString: String,privateKeyString: String) {
        this.publicKeyString = publicKeyString
        this.privateKeyString = privateKeyString
    }

    fun setKeys(jwkProvider: JwkProvider, privateKeyString: String) {
        this.jwkProvider = jwkProvider
        this.privateKeyString = privateKeyString
    }
}


class JWTService {

    var ready: Boolean = false


    lateinit var realm: String
    lateinit var audience: String
    private lateinit var issuer: String
    private lateinit var verifier: JWTVerifier

    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey

    private fun pemToBase64Str(pemKeyStr: String): String{
       return  pemKeyStr.replace("(?m)-----BEGIN.*?-----\\s*".toRegex(), "")
            .replace("(?m)-----END.*?-----\\s*".toRegex(), "")
            .replace("\\s+".toRegex(), "")
    }

    private fun configPublicKey(provider : JwkProvider){
            this.publicKey = provider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey as RSAPublicKey
    }

    private fun configPublicKey(publicKeyString: String){
        try {
            val publicDecoded = Base64.getDecoder().decode(pemToBase64Str(publicKeyString))
            val publicKeySpec = X509EncodedKeySpec(publicDecoded)
            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec) as RSAPublicKey

        }catch (argE: IllegalArgumentException){
            throw AuthException(AuthErrorCodes.INVALID_KEY_FORMAT, "Error while parsing public key: ${argE.message}")
        }
        catch (e:Exception){
            throw AuthException(AuthErrorCodes.UNKNOWN_ERROR, "Error while parsing public key: ${e.message}")
        }
    }

    private fun configPrivateKey(privateKeyString: String){
        try {
            val privateDecoded = Base64.getDecoder().decode(pemToBase64Str(privateKeyString))
            val privateKeySpec = PKCS8EncodedKeySpec(privateDecoded)
            this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec) as RSAPrivateKey

        }catch (argE: IllegalArgumentException){
            throw AuthException(AuthErrorCodes.INVALID_KEY_FORMAT, "Error while parsing private key: ${argE.message}")
        }
        catch (e:Exception){
            throw AuthException(AuthErrorCodes.UNKNOWN_ERROR, "Error while parsing private key: ${e.message}")
        }
    }

    fun configure(config: JwtConfig):JWTService {

        realm = config.realm
        audience = config.audience
        issuer = config.issuer

        if(config.privateKeyString!= null){
            configPrivateKey(config.privateKeyString!!)
        }

        if(config.publicKeyString!= null){
            configPublicKey(config.publicKeyString!!)
            verifier = JWT.require(Algorithm.RSA256(publicKey, null))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()

            ready = true
        }

        if(config.jwkProvider != null){
            configPublicKey(config.jwkProvider!!)
            verifier = JWT.require(Algorithm.RSA256(publicKey, null))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()

            ready = true
        }

        return this
    }

    fun getVerifier(): JWTVerifier {
        if(!::verifier.isInitialized) throw AuthException(AuthErrorCodes.CONFIGURATION_MISSING, "Verifier not initialized. Call configure() first")
        return verifier
    }

    fun generateToken(user: SecureUserContext): String? {
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