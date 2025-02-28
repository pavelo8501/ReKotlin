package po.restwraptor.test.security

import com.auth0.jwt.interfaces.DecodedJWT
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.restwraptor.models.security.JwtConfig
import po.restwraptor.security.JWTService
import po.restwraptor.test.TestRestWraptorSecurity
import po.restwraptor.test.TestUser
import java.io.File
import kotlin.test.assertEquals

class TestJWTService {

    fun extractUserFromToken(decodedJWT:  DecodedJWT): TestUser {

        return TestUser(
            id = decodedJWT.getClaim("id").asLong(),
            login = decodedJWT.getClaim("login").asString(),
            email = decodedJWT.getClaim("email").asString(),
            roles = decodedJWT.getClaim("roles").asList(String::class.java)
        )
    }

    fun jwtConfig(publicKeyString : String, privateKeyString: String):JwtConfig{
        return  JwtConfig(
            realm = "ktor app",
            audience = "jwt-audience",
            issuer = "http://127.0.0.1",
            secret = "secret",
            publicKeyString = publicKeyString,
            privateKeyString = privateKeyString)
    }

    @Test
    fun `jwt token payload encodes user`(){
        var certsPath = File(System.getProperty("user.dir")).toPath().resolve("keys")
        val path = TestRestWraptorSecurity.Companion.certsPath.toString() + File.separator
        val publicPath = File("${path}ktor.spki")
        val privatePath = File("${path}ktor.pk8")
        val config =  jwtConfig(publicPath.readText() ,privatePath.readText())
        val service = JWTService()
        service.init("testService", config)
        val user = TestUser()

        val token = service.generateToken(user)

        val decodedUser = extractUserFromToken(service.decodeToken(service.generateToken(user)))
        assertEquals(user.id, decodedUser.id, "id match")
        assertEquals(user.login, decodedUser.login, "login match")
        assertEquals(user.name, decodedUser.name, "name match")
        assertEquals(user.email, decodedUser.email, "email match")

    }

}