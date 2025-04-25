package po.test.restwraptor

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.readCryptoRsaKeys
import po.auth.extensions.setKeyBasePath
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.extensions.asBearer
import po.restwraptor.extensions.jwtSecured
import po.restwraptor.extensions.respondBadRequest
import po.restwraptor.extensions.stripBearer
import po.restwraptor.extensions.withBaseUrl
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@Serializable
data class TestUser(
    override var id: Long,
    var name : String,
    override var login: String = "someLogin",
    override val hashedPassword: String = "somepassword",
    override var email: String = "some@mail.test",
    var roles: Set<String> = setOf(),
) : AuthenticationPrincipal {

    override val userGroupId: Long = 0
    override fun asJson(): String {
        return Json.encodeToString(this)
    }
}

class TestRestWraptorSecurity {

    private fun  Routing.publicRoutes(){
        post(withBaseUrl("public")) {
            val receivedBody =  call.receive<String>()
            if(receivedBody == "HELO"){
                call.respond(ApiResponse("EHLO"))
            }else{
                respondBadRequest("Expected HELO. Got:$receivedBody")
            }
        }
    }
    private fun  Routing.protectedRoutes(){
        jwtSecured {
            post(withBaseUrl("protected")) {
                println("Call hit /protected")
                val receivedBody =  call.receive<String>()
                if(receivedBody == "HELO"){
                    call.respond(ApiResponse("EHLO"))
                }else{
                    respondBadRequest("Expected HELO. Got:$receivedBody")
                }
            }
        }
    }

    companion object{
        @JvmStatic
        val keyPath = setKeyBasePath("src/test/demo_keys")

        @JvmStatic
        val jsonFormatter = Json{
            ignoreUnknownKeys = true
            isLenient = true
        }

        @JvmStatic
        fun userLookUp(login: String): AuthenticationPrincipal?{
            if(login == "login"){
                return TestUser(0, "someName", "login")
            }else{
                return null
            }
        }
    }

    @OptIn(InternalAPI::class)
    @Test
    fun `Jwt token & session created`()  = testApplication {

        val keyPath = setKeyBasePath("src/test/demo_keys")
        val server = RestWrapTor()
        application {
            server.useApp(this){
                setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::userLookUp)
            }
            routing { publicRoutes(); protectedRoutes() }
        }

        this@testApplication.startApplication()
        val httpClient = this@testApplication.createClient {
            install(ContentNegotiation) { json(jsonFormatter) }
        }

        val publicResponse = httpClient.post("/public") {
            setBody<String>("HELO")
            contentType(ContentType.Application.Json)
        }

        val publicResponseMessage =  publicResponse.body<ApiResponse<String>>().data
        assertEquals(HttpStatusCode.OK, publicResponse.status, "Call did not pass through")
        assertEquals("EHLO", publicResponseMessage, "Wrong response")

        val protectedFailure = httpClient.post("/protected") {
            setBody<String>("HELO")
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Unauthorized, protectedFailure.status, "JwtSecured route pass with no authentication")

        val loginResponse = httpClient.post("/auth/login") {
            setBody<LoginRequest>(LoginRequest("login", "password"))
            contentType(ContentType.Application.Json)
        }

        val sessionId = loginResponse.headers[WraptorHeaders.XAuthToken.value]
        val token =  loginResponse.body<ApiResponse<String>>().data.stripBearer()

        assertAll(
            {assertEquals(HttpStatusCode.OK, loginResponse.status, "Reply status code not 200")},
            {assertNotNull(sessionId, "Session id not provided")},
            {assertNotNull(token, "JWT token not received")},
            { assertNotEquals(sessionId, token,"Token and sessionId received are the same session :$sessionId token: $token")}
        )

        val protectedSuccess = httpClient.post("/protected") {
            val bearerStr = token.asBearer()
            header(HttpHeaders.Authorization, bearerStr)
            header(WraptorHeaders.XAuthToken.value, sessionId)
            setBody<String>("HELO")
            contentType(ContentType.Application.Json)
        }
        val responseMessage =  protectedSuccess.body<ApiResponse<String>>().data
        assertEquals(HttpStatusCode.OK, protectedSuccess.status, "Reply status code not 200")
        assertEquals("EHLO", responseMessage,  "Wrong reply message")
    }

    @Test
    fun `correct authentication exceptions are thrown and processed`()=testApplication {

        val server = RestWrapTor()
        application {
            server.useApp(this){
                setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::userLookUp)
            }
            routing { publicRoutes(); protectedRoutes() }
        }
        this@testApplication.startApplication()
        val httpClient = this@testApplication.createClient {
            install(ContentNegotiation) { json(jsonFormatter) }
        }
        val wrongLoginResponse = httpClient.post("/auth/login") {
            setBody<LoginRequest>(LoginRequest("user2", "password"))
            contentType(ContentType.Application.Json)
        }

        val body = wrongLoginResponse.body<ApiResponse<JsonElement>>()
        assertAll(
            "Wrong login sent",
            {assertEquals(HttpStatusCode.Unauthorized, wrongLoginResponse.status, "wrong reply status code")},
            { assertTrue(body.message.contains("Wrong login"), "wrong error message")}
        )
    }

}