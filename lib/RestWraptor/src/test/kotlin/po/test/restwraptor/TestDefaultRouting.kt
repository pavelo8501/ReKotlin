package po.test.restwraptor

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.exceptions.AuthException
import po.auth.exceptions.ErrorCodes
import po.auth.extensions.authenticate
import po.auth.extensions.readCryptoRsaKeys
import po.auth.extensions.setKeyBasePath
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.launchers.runTask
import po.misc.exceptions.HandlerType
import po.misc.types.getOrThrow
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.extensions.asBearer
import po.restwraptor.extensions.authSessionOrNull
import po.restwraptor.extensions.respondBadRequest
import po.restwraptor.extensions.respondInternal
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.extensions.withBaseUrl
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse

import kotlin.test.assertEquals
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDefaultRouting {

    companion object{
        @JvmStatic
        val keyPath = setKeyBasePath("src/test/demo_keys")

        @JvmStatic
        val jsonFormatter = Json{
            ignoreUnknownKeys = true
            isLenient = true
        }

        var routeHit: Boolean = false

        @JvmStatic
        fun userLookUp(login: String): AuthenticationPrincipal?{
            if(login == "login"){
                return TestUser(0, "someName", "login")
            }else{
                return null
            }
        }
    }
    private fun Routing.publicRoutes() {
        post(withBaseUrl("public")) {
            val receivedBody = call.receive<String>()
            if (receivedBody == "HELO") {
                call.respond(ApiResponse("EHLO"))
            } else {
                respondBadRequest("Expected HELO. Got:$receivedBody")
            }
        }
    }
    private fun Routing.authRoutes() {
        post(withBaseUrl("auth/login")) {
            routeHit = true
            val personalName = "AuthRoutes"
            val loginRoute = "login"

            runTask("Process Post login $personalName $loginRoute") { handler ->
                val session =  call.authSessionOrNull().getOrThrow<AuthorizedSession, AuthException>("Session can not be located", ErrorCodes.SESSION_NOT_FOUND.value)
                handler.handleFailure(HandlerType.SKIP_SELF){ throwable ->
                    println("Error reached")
                    when (throwable) {
                        is AuthException -> {
                            if (throwable.code.value >= 4000 && throwable.code.value < 5000) {
                                respondUnauthorized(throwable.message, throwable.code.value)
                                return@handleFailure
                            } else {
                                respondInternal(throwable.message, throwable.code.value)
                            }
                        }
                        else -> {
                            respondInternal(throwable)
                        }
                    }
                }
                val credentials = call.receive<LoginRequest>()
                val principal = session.authenticate(credentials.login, credentials.password)
                val jwtToken = session.authenticator.jwtService.generateToken(principal, session)
                call.response.header(HttpHeaders.Authorization, jwtToken.token.asBearer())
                call.response.header(WraptorHeaders.XAuthToken.value, session.sessionID)

                handler.info("Header ${HttpHeaders.Authorization} set value: ${jwtToken.token.asBearer()}")
                handler.info("Header ${WraptorHeaders.XAuthToken.value} set value: ${session.sessionID}")
                call.respond(ApiResponse(jwtToken.token))
            }.resultOrException()
        }
    }

    @Test
    fun `Can override default authentication routes`() = testApplication {

        val keyPath = setKeyBasePath("src/test/demo_keys")
        val server = RestWrapTor()
        application {
            server.useApp(this) {
                apiConfig.systemRouts = false
                authConfig.defaultSecurityRouts = false
                setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::userLookUp)
                routing {
                    publicRoutes()
                    authRoutes()
                }
            }
        }
        this@testApplication.startApplication()
        val httpClient = this@testApplication.createClient {
            install(ContentNegotiation) { json(jsonFormatter) }
        }
        val response = httpClient.post("auth/login") {
            setBody<LoginRequest>(LoginRequest("login", "password"))
            contentType(ContentType.Application.Json)
        }
        assertTrue(routeHit, "Rout never hit")
        assertEquals(HttpStatusCode.OK, response.status, "JwtSecured route pass with no authentication")
    }
}