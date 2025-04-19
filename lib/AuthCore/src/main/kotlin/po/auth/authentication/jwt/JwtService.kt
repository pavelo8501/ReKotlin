package po.auth.authentication.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlinx.serialization.json.Json
import po.auth.authentication.Authenticator
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.extensions.getOrThrow
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.authentication.jwt.models.JwtToken
import po.auth.authentication.jwt.repositories.InMemoryTokenStore
import po.auth.sessions.extensions.authorizedSession
import po.auth.sessions.models.AuthorizedPrincipal
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import java.time.Instant
import java.util.Date

class JWTService(
    private var config : JwtConfig
): TasksManaged {
    var name: String = ""

    val realm: String
        get(){return config.realm}

    val tokenRepository = InMemoryTokenStore()


    private var authenticationFn: (suspend (login: String, password: String)-> AuthorizedPrincipal) ? = null

    suspend fun setAuthenticationFn(callback : (suspend (login: String, password: String)-> AuthorizedPrincipal)){
        authenticationFn = callback
        Authenticator.setAuthenticator(callback)
    }

    private val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.RSA256(config.publicKey, null))
        .withAudience(config.audience)
        .withIssuer(config.issuer)
        .build()

    private fun cleanToken(token: String): String{
        return token.removePrefix("Bearer").trim()
    }

    suspend fun generateToken(user: AuthorizedPrincipal): JwtToken {
       val serialized = user.asJson()
       val token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("user_json", serialized)
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(Algorithm.RSA256(null, config.privateKey))
        val sessionId = authorizedSession(user).sessionId
        return tokenRepository.store(JwtToken(token, sessionId))
    }

    fun getVerifier(): JWTVerifier{
        return jwtVerifier
    }

    fun decodeToken(token: String): DecodedJWT =  jwtVerifier.verify(cleanToken(token))

    fun checkCredential(credential: JWTPrincipal): JWTPrincipal?{
        val user =  credential.payload.getClaim("user_json").asString()
        return if(user != null){
            JWTPrincipal(credential.payload)
        }else{
            null
        }
    }

    suspend fun checkCredential(credential: JWTCredential): JWTPrincipal?{

        subTask("CheckCredential", "JWTService"){handler->
            handler.info("Checking credentials for ${credential.payload}")
        }

        val user =  credential.payload.getClaim("user_json").asString()
        return if(user != null){
            JWTPrincipal(credential.payload)
        }else{
            null
        }
    }

//    suspend fun checkCredential(sessionId: String, credential : JWTPrincipal): JWTPrincipal?{
//
//        val user =  credential.payload.getClaim("user_json").asString()
//        if(user != null){
//           return JWTPrincipal(credential.payload)
//        }
//
//        val jwtToken = tokenRepository.resolve(sessionId).getOrThrow("SessionId: $sessionId not found", ErrorCodes.ABNORMAL_STATE)
//        decodeToken(jwtToken.token).let { decoded ->
//            runCatching {
//                val user = Json.decodeFromString<AuthorizedPrincipal>(decoded.payload)
//                val token = generateToken(user)
//            }.onFailure {
//                return null
//            }
//        }
//        return null
//    }

    suspend fun isValid(
        jwtString: JwtToken,
        invalidationFn : suspend  (JwtToken)-> Unit
    ): Boolean{
        val decodedJWT = decodeToken(jwtString.token)
        val expirationLong = decodedJWT.expiresAt?.time ?: 0
        val expirationTime = Instant.ofEpochMilli(expirationLong)
        if (!expirationTime.isBefore(Instant.now())) {
            return true
        }else{
            invalidationFn.invoke(jwtString)
            return false
        }
    }
}