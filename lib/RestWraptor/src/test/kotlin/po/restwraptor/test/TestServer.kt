package po.restwraptor.test

import io.ktor.server.application.Application
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import po.restwraptor.RestServer
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.plugins.RateLimiterPlugin
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}