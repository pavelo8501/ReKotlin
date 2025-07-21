package po.auth.authentication.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.authentication.jwt.models.JwtToken
import po.auth.authentication.jwt.repositories.InMemoryTokenStore
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.misc.context.Identifiable
import java.time.Instant
import java.util.Date

class JWTService(
    private var config : JwtConfig
){

    var name: String = ""
    val realm: String
        get(){return config.realm}

    val tokenRepository = InMemoryTokenStore()

    suspend fun setAuthenticationFn(userLookupFn : (suspend (login: String)-> AuthenticationPrincipal?)){
        AuthSessionManager.registerAuthenticator(userLookupFn)
    }

    private val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.RSA256(config.publicKey, null))
        .withAudience(config.audience)
        .withIssuer(config.issuer)
        .build()

    private fun cleanToken(token: String): String{
        return token.removePrefix("Bearer").trim()
    }

    suspend fun generateToken(principal: AuthenticationPrincipal,  session : AuthorizedSession): JwtToken {
       val serialized = principal.asJson()
       val token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withSubject(principal.id.toString())
            .withClaim("session_id", session.sessionID)
                .withClaim("user_json", serialized)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(Algorithm.RSA256(null, config.privateKey))

        return tokenRepository.store(session.sessionID, token, 0L)
    }

    fun getVerifier(): JWTVerifier{
        return jwtVerifier
    }

    fun decodeToken(token: String): DecodedJWT =  jwtVerifier.verify(cleanToken(token))

    fun validateToken(credential: JWTCredential): JWTPrincipal?{
        val user =  credential.payload.getClaim("user_json").asString()
        return if(user != null){
            JWTPrincipal(credential.payload)
        }else{
            null
        }
    }

    fun validateToken(jwtToken: JwtToken?): JWTPrincipal? {
        if(jwtToken == null){
            return null
        }
        return try {
            val decoded = decodeToken(jwtToken.token)
            JWTPrincipal(decoded)
        } catch (e: JWTVerificationException) {
            null
        }
    }

    suspend fun isNotExpired(
        jwtString: JwtToken?,
        invalidationFn : suspend  (JwtToken)-> Unit
    ): Boolean{
        if(jwtString == null){
            return false
        }
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