package po.test.restwraptor

import io.ktor.client.request.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.restwraptor.RestWrapTor
import po.restwraptor.configureWraptor
import po.restwraptor.routes.ManagedRouting
import po.restwraptor.routes.withBaseUrl
import po.restwraptor.scope.ConfigContext


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSessionHandling {

    val server get() = RestWrapTor()

    fun userLookup(login: String): AuthenticationPrincipal {
        return TestUser(0, "someName", login)
    }

    fun ManagedRouting.configManagedRoutes() {

        managedRoutes {
            managedGet("default"){session->
                call.respond("OK")
            }

            managedGet("default2"){session->

                session.output()
                session.roundTripInfo.forEach {
                    it.output(Colour.CYAN)
                }

                call.respond("OK")
            }
        }
    }

    fun Routing.securedRoute(){
        post(withBaseUrl("auth/login")) {

        }
    }

    @Test
    fun `Default session is created on incomming call`() = testApplication {

        application {
            configureWraptor(server){
                setupRoutes {
                    configManagedRoutes()
                }
            }
            routing {
            }
        }
        startApplication()

        val httpClient = createClient {

        }
        val defaultResponse = httpClient.get("/default") {
        }

        val defaultResponse2 = httpClient.get("/default2") {

        }


    }


//
//    @Test
//    fun `session applied and remains during whole call length`() = testApplication {
//
//        val keyPath = setKeyBasePath("src/test/demo_keys")
//        val server = RestWrapTor()
//        val jsonFormatter = Json {
//            ignoreUnknownKeys = true
//            isLenient = true
//            encodeDefaults = true
//        }
//
//        var protectedCallSessionId : String? = null
//
//        application {
//            configureWraptor(server){
//
//                responseProvider.provideValue {
//                    DefaultResponse("")
//                }
//
//                setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::userLookup)
//            }
//            routing {
//               jwtSecured {
//                   securedRoute()
//               }
//            }
//        }
//
//        startApplication()
//        val httpClient = this@testApplication.createClient {
//            install(ContentNegotiation) { json() }
//        }
//
//        val loginResponse = httpClient.post("/auth/login") {
//            setBody<LoginRequest>(LoginRequest("user", "password"))
//            contentType(ContentType.Application.Json)
//        }
//
//        val token = loginResponse.headers[HttpHeaders.Authorization].stripBearer()
//        val sessionId = loginResponse.headers[WraptorHeaders.XAuthToken.value]
//
//        assertNotNull(token, "Token not received")
//        assertNotNull(sessionId, "SessionId not received")
//
//        val authenticatedResponse = httpClient.post("/protected") {
//            val withBarer = token.asBearer()
//            println("Token in test $withBarer")
//            header(HttpHeaders.Authorization, withBarer)
//            header(WraptorHeaders.XAuthToken.value, sessionId)
//            setBody<LoginRequest>(LoginRequest("user", "password"))
//            contentType(ContentType.Application.Json)
//        }
//
//        val roundTripSessionId = authenticatedResponse.headers[WraptorHeaders.XAuthToken.value]
//        assertEquals(HttpStatusCode.OK, authenticatedResponse.status, "Reply status code not 200")
//        assertNotNull(protectedCallSessionId, "Protected rout never hit")
//        assertEquals(sessionId, protectedCallSessionId, "Wrong protectedCallSessionId")
//        assertEquals(protectedCallSessionId, roundTripSessionId, "Wrong roundTripSessionId SessionId")
//    }
}