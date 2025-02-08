package po.restwraptor.security

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlinx.coroutines.runBlocking
import po.lognotify.shared.enums.HandleType
import po.restwraptor.exceptions.AuthErrorCodes
import po.restwraptor.exceptions.AuthException
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.security.JwtConfig
import po.restwraptor.plugins.JWTPlugin.Plugin.service
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date

class JWTService{

    private lateinit var  config : JwtConfig
    var isReady: Boolean = false
        private set
    var name: String = ""

    val realm: String
        get(){return config.realm}
    val claim: String
        get(){return config.claimFieldName}

    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey
    private lateinit var verifier : JWTVerifier


    fun init(serviceName: String, conf: JwtConfig){
        if(isReady){
            return
        }
        config = conf
        name = serviceName
            config.privateKeyString?.let { loadPrivateKey(it) }
        when {
            config.publicKeyString != null -> {
                loadPublicKey(config.publicKeyString!!)
            }
            config.jwkProvider != null &&  config.kid != null -> {
                loadPublicKeyRSA(config.jwkProvider!!, config.kid!!)
            }
            config.jwkProvider == null &&  config.kid == null && config.publicKeyString == null ->{
                throw AuthException(AuthErrorCodes.CONFIGURATION_MISSING, "No public key configuration provided.")
            }
        }

        verifier = JWT.require(Algorithm.RSA256(publicKey, null))
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .build()
        isReady = true
    }

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

    private fun loadPublicKeyRSA(provider: JwkProvider, token: String) {
        try {
            val jwt = JWT.decode(token) // Decode JWT to get key ID (kid)
            val keyId = jwt.keyId ?: throw AuthException(
                AuthErrorCodes.CONFIGURATION_MISSING, "JWT key ID (kid) missing"
            )
            this.publicKey = provider.get(keyId).publicKey as RSAPublicKey
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

    private fun cleanToken(token: String): String{
       return token.removePrefix("Bearer").trim()
    }

    fun generateToken(user: SecuredUserInterface): String =
        JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim(config.claimFieldName, user.login)
            .withClaim("user", user.toPayload())
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(Algorithm.RSA256(null, privateKey))

    @JvmName("getServiceVerifier")
    fun getVerifier(): JWTVerifier{
        if(!::verifier.isInitialized) throw ConfigurationException("Verifier not initialized. Call configure() first",
            HandleType.PROPAGATE_TO_PARENT)
        return verifier
    }

    fun decodeToken(token: String): DecodedJWT =
        try {
            verifier.verify(cleanToken(token))
        } catch (ex: JWTDecodeException) {
            throw AuthException(AuthErrorCodes.INVALID_TOKEN, "Error while verifying token: ${ex.message}")
        }

    fun checkCredential(credential: JWTCredential): JWTPrincipal?{
        val claim =  credential.payload.getClaim(service.claim).asString()
        return if(claim != null){
            JWTPrincipal(credential.payload)
        }else{
            null
        }
    }

    fun checkExpiration(
        jwtString: String,
        headerUpdateFn : suspend  (String?)-> Unit
    ){
        val decodedJWT = decodeToken(jwtString)
        val expirationLong = decodedJWT.expiresAt?.time ?: 0
        val expirationTime =  Instant.ofEpochMilli(expirationLong)
        if(expirationTime.isBefore(Instant.now())){
            val user = decodedJWT.getClaim("user").asString()
            val userInterface =   SecuredUserInterface.fromPayload(user)
            if(userInterface!= null){
                val newToken =  this@JWTService.generateToken(userInterface)
                runBlocking {
                    headerUpdateFn.invoke(newToken)
                }
            }
        }
    }
}