package po.test.restwraptor

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import po.auth.extensions.asBearer
import po.auth.extensions.readCryptoRsaKeys
import po.auth.extensions.setKeyBasePath
import po.auth.extensions.stripBearer
import po.auth.sessions.models.AuthorizedPrincipal
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.extensions.jwtSecured
import po.restwraptor.extensions.withBaseUrl
import po.restwraptor.extensions.withSession
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import kotlin.test.assertEquals

class TestSessionHandling {

    fun authenticate(login: String, password: String): AuthorizedPrincipal{
        return TestUser(0, "someName", login, password).asAuthorizedPrincipal()
    }

    @Test
    fun `session applied and remains during whole call length`() = testApplication {

        val keyPath = setKeyBasePath("src/test/demo_keys")
        val server = RestWrapTor()
        val jsonFormatter = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        var protectedCallSessionId : String? = null

        application {
            server.useApp(this) {
                setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::authenticate) {

                }
            }
            routing {
               jwtSecured {
                   post(withBaseUrl("protected")){
                       try {
                           call.withSession {
                               protectedCallSessionId = sessionId
                               call.response.header(WraptorHeaders.XAuthToken.value, sessionId)
                               call.respond(ApiResponse("Response"))
                           }
                       }catch (th: Throwable){
                           println(th.message)
                       }
                   }
               }
            }
        }

        startApplication()
        val httpClient = this@testApplication.createClient {
            install(ContentNegotiation) { json() }
        }

        val loginResponse = httpClient.post("/auth/login") {
            setBody<LoginRequest>(LoginRequest("user", "password"))
            contentType(ContentType.Application.Json)
        }

        val token = loginResponse.headers[HttpHeaders.Authorization].stripBearer()
        val sessionId = loginResponse.headers[WraptorHeaders.XAuthToken.value]

        assertNotNull(token, "Token not received")
        assertNotNull(sessionId, "SessionId not received")

        val authenticatedResponse = httpClient.post("/protected") {
            val withBarer = token.asBearer()
            println("Token in test $withBarer")
            header(HttpHeaders.Authorization, withBarer)
            header(WraptorHeaders.XAuthToken.value, sessionId)
            setBody<LoginRequest>(LoginRequest("user", "password"))
            contentType(ContentType.Application.Json)
        }

        val roundTripSessionId = authenticatedResponse.headers[WraptorHeaders.XAuthToken.value]
        assertEquals(HttpStatusCode.OK, authenticatedResponse.status, "Reply status code not 200")
        assertNotNull(protectedCallSessionId, "Protected rout never hit")
        assertEquals(sessionId, protectedCallSessionId, "Wrong protectedCallSessionId")
        assertEquals(protectedCallSessionId, roundTripSessionId, "Wrong roundTripSessionId SessionId")
    }
}