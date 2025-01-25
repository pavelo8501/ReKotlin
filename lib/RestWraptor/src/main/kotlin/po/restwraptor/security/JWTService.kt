package po.restwraptor.security

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import po.restwraptor.exceptions.AuthErrorCodes
import po.restwraptor.exceptions.AuthException
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.security.JwtConfig
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.Date

class JWTService {

    var isReady: Boolean = false
        private set

    lateinit var realm: String
        private set
    lateinit var audience: String
        private set
    lateinit var issuer: String
        private set

    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey
    private lateinit var verifier: JWTVerifier

    private fun parsePemKey(pemKeyStr: String): ByteArray =
        pemKeyStr
            .replace("(?m)-----BEGIN.*?-----\\s*".toRegex(), "")
            .replace("(?m)-----END.*?-----\\s*".toRegex(), "")
            .replace("\\s+".toRegex(), "")
            .let { Base64.getDecoder().decode(it) }

    private fun loadPublicKey(publicKeyString: String) {
        try {
            val publicKeySpec = X509EncodedKeySpec(parsePemKey(publicKeyString))
            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec) as RSAPublicKey
        } catch (e: Exception) {
            throw AuthException(AuthErrorCodes.INVALID_KEY_FORMAT, "Error while parsing public key: ${e.message}")
        }
    }

    private fun loadPublicKey(provider: JwkProvider) {
        try {
            this.publicKey = provider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey as RSAPublicKey
        } catch (e: Exception) {
            throw AuthException(AuthErrorCodes.UNKNOWN_ERROR, "Error while loading public key: ${e.message}")
        }
    }

    private fun loadPrivateKey(privateKeyString: String) {
        try {
            val privateKeySpec = PKCS8EncodedKeySpec(parsePemKey(privateKeyString))
            this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec) as RSAPrivateKey
        } catch (e: Exception) {
            throw AuthException(AuthErrorCodes.INVALID_KEY_FORMAT, "Error while parsing private key: ${e.message}")
        }
    }

    fun configure(config: JwtConfig): JWTService {
        realm = config.realm
        audience = config.audience
        issuer = config.issuer

        config.privateKeyString?.let { loadPrivateKey(it) }

        when {
            config.publicKeyString != null -> {
                loadPublicKey(config.publicKeyString!!)
            }
            config.jwkProvider != null -> {
                loadPublicKey(config.jwkProvider!!)
            }
            else -> throw AuthException(AuthErrorCodes.CONFIGURATION_MISSING, "No public key configuration provided.")
        }

        verifier = JWT.require(Algorithm.RSA256(publicKey, null))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()

        isReady = true
        return this
    }

    fun getVerifier(): JWTVerifier =
        if (::verifier.isInitialized) verifier
        else throw AuthException(
            AuthErrorCodes.CONFIGURATION_MISSING,
            "Verifier not initialized. Call configure() first"
        )

    fun generateToken(user: SecuredUserInterface): String =
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .withPayload(user.toPayload())
            .sign(Algorithm.RSA256(null, privateKey))

    fun verifyToken(token: String): DecodedJWT =
        try {
            getVerifier().verify(token)
        } catch (e: Exception) {
            throw AuthException(AuthErrorCodes.INVALID_TOKEN, "Error while verifying token: ${e.message}")
        }
}