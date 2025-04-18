package po.test.restwraptor

import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingRoot
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.restwraptor.RestWrapTor
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class TestResponse(
    val msg: String = "Some message",
    val number: Int = 10
)


class TestRestWrapTor {

    @Test
    fun `test server when uninitialized`(){
        val apiServer = RestWrapTor()
        val serverConfig =  apiServer.getConfig()
        assertEquals(false, apiServer.initialized)
        assertNull(serverConfig)
    }

    @Test
    fun `test server initialize application itself`() {
        assertThrows<IllegalStateException>("EmbeddedServer was stopped"){
            val apiServer = RestWrapTor()
            apiServer.start{handler->
                handler.stop()
            }
            apiServer.start("0.0.0.0",8080, false)
        }
    }

    @Test
    fun `test server use pre-init application`() = testApplication {

        val apiServer = RestWrapTor()
        this@testApplication.application{
            val hashOfTestApp = System.identityHashCode(this)
            install(RoutingRoot)
            routing {
                get("test/getRoute") { call.respond("Hello, from getRoute!") }
            }
            apiServer.applyConfig {this@application}

            val routes = apiServer.getRoutes().map { it.path }

            assertEquals(hashOfTestApp, apiServer.appHash)
            assertContains(routes, "/test/getRoute")
        }
    }

    @Test
    fun `test server applies configuration to pre-init application`() = testApplication {
        val apiServer =  RestWrapTor{
              this@RestWrapTor.setupApplication {
                  this@setupApplication.routing {
                      get("test/route") { call.respond("Hello, from route!") }
                  }
              }
        }
        this@testApplication.application{
            routing {
                get("test/preConfigRoute") { call.respond("Hello, from preConfigRoute!") }
            }
            apiServer.applyConfig {this}
           // apiServer.usePreconfiguredApp(this)
        }
        startApplication()
        val routes = apiServer.getRoutes().map { it.path }
        assertContains(routes, "/test/route")
        assertContains(routes, "/test/preConfigRoute")
    }

    @Test
    fun `test features can be switched off`() = testApplication {
        this@testApplication.application{
            val apiServer =  RestWrapTor{
                this@RestWrapTor.setup {
                    this@RestWrapTor.apiConfig.also {
                        it.cors = false
                        it.contentNegotiation = false
                        it.systemRouts = false
                    }
                }
            }
            apiServer.applyConfig {
                this
            }
            val corsPlugin = this@application.pluginOrNull(CORS)
            val contentNegotiationPlugin = this@application.pluginOrNull(ContentNegotiation)

            assertNull(corsPlugin)
            assertNull(contentNegotiationPlugin)
        }
    }

}