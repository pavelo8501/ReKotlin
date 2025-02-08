package po.restwraptor.test

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import po.restwraptor.RestWrapTor
import po.restwraptor.extensions.configServer
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.response.ApiResponse
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@Serializable
data class TestUser(
    override var id: Long = 0,
    val name : String = "someName",
    val password : String = "somePassword",
    override var login: String = "someLogin",
    override var roles: List<String>  = listOf<String>()
) : SecuredUserInterface{
    override fun toPayload(): String {
        return """{"id":${id}, "name":"${name}", "login":"${login}" }"""
    }
}

class TestRestWraptorSecurity {

    companion object {
        var certsPath = File(System.getProperty("user.dir")).toPath().resolve("keys")

        val apiServer =  RestWrapTor()
        lateinit var httpClient : HttpClient

        private fun authFunction(login : String, password : String): TestUser?{
            return if (login == "someLogin" && password == "somePassword") {
                TestUser()
            }else{
                null
            }
        }

        val jsonConverter = Json{
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        @BeforeAll
        @JvmStatic
        fun setup(){


        }
    }

    @Test
    fun `test if default security handling routes configured correctly`() = testApplication {

        application {
            apiServer.usePreconfiguredApp(this)
            this@application.configServer {
                setupAuthentication {
                    val path = certsPath.toString() + File.separator
                    setKeyPath(path).applySecurity("ktor.spki", "ktor.pk8"){ authFunction(it.login, it.password) }
                }
            }
            routing {
                authenticate("jwt-auth") {
                    get("/api/protected") { call.respond(HttpStatusCode.OK, "Access Granted") }
                }
            }
        }

        startApplication()
        val routes = apiServer.getRoutes().map { it.path }

        assertContains(routes, "/api/login")
        assertContains(routes, "/api/refresh")
        assertContains(routes, "/api/protected")

        httpClient = createClient {
            install(ContentNegotiation){
                json()
            }
        }


        val loginResponse = httpClient.post ("/api/login") {
           // setBody("""{"login":"testUser", "password":"testPass"}""")
            setBody(ApiRequest<TestUser>(TestUser()))
            contentType(ContentType.Application.Json)
        }

        val token = loginResponse.headers["Authorization"]
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        assertNotNull(loginResponse.headers["Authorization"], "JWT token present in header")
        assertNotNull(token, "JWT token should be issued")

        // Step 2: Try accessing /api/protected WITHOUT a token (should fail)
        val unauthorizedResponse = client.get("/api/protected")
        assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)

        // Step 3: Try accessing /api/protected WITH a token (should pass)
        val authorizedResponse = client.get("/api/protected") {
            header(HttpHeaders.Authorization, token)
        }
        assertEquals(HttpStatusCode.OK, authorizedResponse.status)
        assertEquals("Access Granted", authorizedResponse.bodyAsText())

    }
}