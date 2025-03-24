package po.auth.authentication.tokens.jwt

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.jwt.JWTPrincipal
import po.auth.authentication.interfaces.SerializablePrincipal
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date

class JWTService(
    private var config : JwtConfig
) {
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


    private fun loadPrivateKey(privateKeyString: String) {
        val privateKeySpec = PKCS8EncodedKeySpec(parsePemKey(privateKeyString))
        this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec) as RSAPrivateKey
    }

    private fun cleanToken(token: String): String{
        return token.removePrefix("Bearer").trim()
    }

    private fun loadPublicKeyRSA(provider: JwkProvider, token: String) {
        val jwt = JWT.decode(token) // Decode JWT to get key ID (kid)
        val keyId = jwt.keyId ?: throw Exception("JWT key ID (kid) missing")
        this.publicKey = provider.get(keyId).publicKey as RSAPublicKey
    }

    private fun loadPublicKey(publicKeyString: String) {
        val publicKeySpec = X509EncodedKeySpec(parsePemKey(publicKeyString))
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec) as RSAPublicKey
    }

    private fun parsePemKey(pemKeyStr: String): ByteArray =
        pemKeyStr
            .replace("(?m)-----BEGIN.*?-----\\s*".toRegex(), "")
            .replace("(?m)-----END.*?-----\\s*".toRegex(), "")
            .replace("\\s+".toRegex(), "")
            .let { Base64.getDecoder().decode(it) }

    fun init(serviceName: String, conf: JwtConfig? = null){
        if(isReady){
            return
        }
        name = serviceName
        if(conf != null){
            config = conf
        }

       if(config.jwkProvider != null &&  config.kid != null){
            loadPublicKeyRSA(config.jwkProvider!!, config.kid!!)
        }

        verifier = JWT.require(Algorithm.RSA256(publicKey, null))
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .build()
        isReady = true
    }

    fun loadKeys(privateKey: String, publicKey: String): Pair<String, String> {
        return Pair(privateKey, publicKey)
    }

    fun generateToken(user: SerializablePrincipal): String =
        JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim(config.claimFieldName, user.username)
            .withClaim("user_json", user.asJson())
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(Algorithm.RSA256(null, privateKey))

    @JvmName("getServiceVerifier")
    fun getVerifier(): JWTVerifier{

        if(::verifier.isInitialized){
            return  verifier
        }else{
            throw Exception("JWTVerifier is undefined")
        }
    }

    fun decodeToken(token: String): DecodedJWT =  verifier.verify(cleanToken(token))

    fun checkCredential(credential: JWTPrincipal): JWTPrincipal?{
        val claim =  credential.payload.getClaim(claim).asString()
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
        val expirationTime = Instant.ofEpochMilli(expirationLong)
        if (!expirationTime.isBefore(Instant.now())) {
            //Unvalidate
        }
    }
}