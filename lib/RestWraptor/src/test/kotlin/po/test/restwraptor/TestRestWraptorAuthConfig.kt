package po.test.restwraptor

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.extensions.getOrThrow
import po.auth.authentication.extensions.readCryptoRsaKeys
import po.auth.authentication.extensions.setKeyBasePath
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.models.AuthorizedPrincipal
import po.restwraptor.RestWrapTor
import po.restwraptor.extensions.decodeApiResponse
import po.restwraptor.extensions.jwtSecured
import po.restwraptor.extensions.respondBadRequest
import po.restwraptor.extensions.withBaseUrl
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import kotlin.test.assertContains
import kotlin.test.assertEquals

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

    override fun asJson(): String {
        return Json.encodeToString(this)
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

    suspend fun authenticateFailed(login: String, password: String): AuthorizedPrincipal{
      throw AuthException("Authentication failed", ErrorCodes.INVALID_CREDENTIALS)
    }

    private fun  Routing.publicRoutes(){
        post(withBaseUrl("public")) {
            val receivedBody =  call.receive<String>()

            if(receivedBody == "HELO"){
                call.respond<String>("EHLO")
            }else{
                respondBadRequest("Expected HELO. Got:$receivedBody")
            }
        }
    }

    private fun  Routing.protectedRoutes(){
        jwtSecured {
            post(withBaseUrl("protected")) {
                val receivedBody =  call.receive<String>()
                if(receivedBody == "HELO"){
                    call.respond<String>("EHLO")
                }else{
                    respondBadRequest("Expected HELO. Got:$receivedBody")
                }
            }
        }
    }

    @OptIn(InternalAPI::class)
    @Test
    fun `Jwt token & session created`()  = testApplication {

        val keyPath = setKeyBasePath("src/test/demo_keys")
        val server = RestWrapTor()

        val jsonFormatter = Json{
            ignoreUnknownKeys = true
            isLenient = true
        }

        application {
            server.useApp(this){
                setupAuthentication{
                    jwtConfig(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::authenticate)
                }
            }
            routing {
                publicRoutes()
                protectedRoutes()
            }
        }

        this@testApplication.startApplication()

        val httpClient = this@testApplication.createClient {
            install(ContentNegotiation) { json() }
        }

        val publicResponse = httpClient.post("/public") {
            setBody<String>("HELO")
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, publicResponse.status, "Call did not pass through")
        assertEquals("EHLO", publicResponse.body(), "Wrong response")

        val protectedFailure = httpClient.post("/protected") {
            setBody<String>("HELO")
            contentType(ContentType.Application.Json)
        }

        val bodyText = protectedFailure.bodyAsText()
        val jsonObject = decodeApiResponse<ApiResponse<JsonElement>>(bodyText).getOrThrow("ssss", ErrorCodes.INVALID_CREDENTIALS)
        val message =  jsonObject.message

        assertEquals(HttpStatusCode.Unauthorized, protectedFailure.status, "JwtSecured route pass with no authentication")
        assertContains(message, "Header", true, "Wrong failure message. ")


        val loginResponse = httpClient.post("/auth/login") {
            setBody<LoginRequest>(LoginRequest("user", "password"))
            contentType(ContentType.Application.Json)
        }

        val token =  loginResponse.body<ApiResponse<String>>()
        val sessionId = loginResponse.headers["X-Auth"]

        assertAll(
            {assertEquals(HttpStatusCode.OK, loginResponse.status, "Reply status code not 200")},
            {assertNotNull(sessionId, "Session id not provided")},
            {assertNotNull(token, "JWT token not received")}
        )

        val protectedSuccess = httpClient.post("/protected") {
            headers["X-Auth"] = sessionId.toString()
            setBody<String>("HELO")
            contentType(ContentType.Application.Json)
        }

        val responseMessage =  protectedSuccess.body<ApiResponse<String>>().data

        assertEquals(HttpStatusCode.OK, protectedSuccess.status, "Reply status code not 200")
        assertEquals("EHLO", responseMessage,  "Wrong reply message")


    }

}