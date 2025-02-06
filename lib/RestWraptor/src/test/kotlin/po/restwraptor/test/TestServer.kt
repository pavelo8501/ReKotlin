package po.restwraptor.test

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import po.restwraptor.RestServer
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.plugins.RateLimiterPlugin
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Serializable
data class TestResponse(
    val msg: String = "Some message",
    val number: Int = 10
)


class TestServer {

    @Test
    fun `test serve configurations applied correctly`() = testApplication {
        val currentDir = File("").absolutePath
        val keyDir = currentDir+File.separator+"keys"+File.separator
        application {
           val server = RestServer(this){
                setupApi {
                    setAuthKeys(
                        publicKey  =  File(keyDir+"ktor.spki").readText(),
                        privateKey =  File(keyDir+"ktor.pk8").readText()
                    )
                    baseApiRoute = "testapi/"
                }
           }
           server.getApp().apply {
                assertNotNull(pluginOrNull(CORS))
                assertNotNull(pluginOrNull(ContentNegotiation))
                assertNotNull(pluginOrNull(RateLimiterPlugin))
                assertNotNull(pluginOrNull(Authentication))
                assertNotNull(pluginOrNull(JWTPlugin))
                assertEquals("testapi/", server.getConfig().baseApiRoute)
            }
        }
    }

    @Test
    fun `test Rest routing`() = testApplication {

         var server :RestServer? = null
         application {
            server =  RestServer(this){
                setupApi {
                    this.enableDefaultSecurity = false
                }
                setupApplication{
                   this.routing {
                       get("/testRoute") {
                           call.respond(TestResponse())
                       }
                   }
                }
            }
        }
        server?.start()

       val client = this.createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation){
                json()
            }
        }
        val response = client.get("/testRoute")
        assertEquals(HttpStatusCode.OK, response.status)
        val decodedResponse = Json.decodeFromString<TestResponse>(response.bodyAsText())
        assertEquals("Some message", decodedResponse.msg)
    }

}