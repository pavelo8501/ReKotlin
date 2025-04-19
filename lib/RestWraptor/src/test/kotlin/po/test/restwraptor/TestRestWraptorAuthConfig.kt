package po.test.restwraptor

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import po.auth.authentication.extensions.readCryptoRsaKeys
import po.auth.authentication.extensions.setKeyBasePath
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.models.AuthorizedPrincipal
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.RouteSelector
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.server.WraptorRoute
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


@Serializable
data class TestUser(
    var id: Long,
    var name : String,
    override var login: String = "someLogin",
    var password : String = "somePassword",
    override var email: String = "some@mail.test",
    override var roles: Set<String> = setOf(),
) : AuthenticationPrincipal {

    override val userId: Long = id
    override val userGroupId: Long = 0
    fun initBy(authorized : AuthorizedPrincipal){
        id = authorized.userId
        login = authorized.login
        roles = authorized.roles
    }
    fun asAuthorizedPrincipal():AuthorizedPrincipal{
      return  AuthorizedPrincipal(id, login, email, userGroupId, roles)
    }
}


class TestRestWraptorAuthConfig {

    @Test
    fun `RSA key generation`(){
        // val keys = generateRsaKeys()
        // keys.writeToDisk("keys/")
    }

    suspend fun authenticate(login: String, password: String): AuthorizedPrincipal{
        return TestUser(0, "someName", login, password).asAuthorizedPrincipal()
    }

    suspend fun authenticateNotFound(login: String, password: String): AuthorizedPrincipal{
      throw Exception("Authentication failed")
    }

    @Test
    fun `Jwt token is issued user session is created`()  = testApplication {

        val keyPath = setKeyBasePath("src/test/demo_keys")
        val server = RestWrapTor()

        application {
            server.applyConfig {
                setupAuthentication{
                    jwtConfig(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::authenticate)
                }
            }
        }

        this@testApplication.startApplication()
        val httpClient = this@testApplication.createClient {
            install(ContentNegotiation) { json() }
        }

        val loginResponse = httpClient.post("/auth/login") {
            val requestData = ApiRequest(LoginRequest("user", "password"))
            setBody<ApiRequest<LoginRequest>>(requestData)
            contentType(ContentType.Application.Json)
        }
        val token = loginResponse.headers["Authorization"]

        assertEquals(HttpStatusCode.OK, loginResponse.status, "Reply status code not 200")
        assertNotNull(loginResponse.headers["Authorization"], "JWT token present in header")
        assertNotNull(token, "JWT token should be issued")
    }

    @Test
    fun `JWT Token auth plugin installed and routes active`(){
        val keyPath = setKeyBasePath("src/test/demo_keys")
        val securedRoutes : MutableList<WraptorRoute> = mutableListOf()
        val server = RestWrapTor{
            setupAuthentication {
                jwtConfig(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::authenticate)
            }
        }
        server.start{handler->
            securedRoutes.addAll(handler.getRoutes())
            handler.stop()
        }

        assertAll(
            { assertNotEquals(0, securedRoutes.count(), "No default routes installed") },
            { assertTrue(securedRoutes.any { it.path == "/auth/login" && it.selector == RouteSelector.POST }, "Post login route not present") },
            { assertTrue(securedRoutes.any { it.path == "/auth/refresh" && it.selector == RouteSelector.POST }, "Post refresh route not present") },
        )
    }

}