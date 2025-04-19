package po.test.restwraptor

import io.ktor.server.application.PluginInstance
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.EnvironmentType
import po.restwraptor.enums.RouteSelector
import po.restwraptor.extensions.getWraptorRoutes
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.server.WraptorRoute
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestRestWraptorGeneralConfig {

    @Test
    fun `default configuration installs and configures all plugins`() = testApplication {

        val routes : MutableList<WraptorRoute> = mutableListOf()
        val server = RestWrapTor()

        application {
            server.useApp(this){

            }
        }
        startApplication()
        routes.addAll(server.getRoutes())
        assertAll(
            { assertNotEquals(0, routes.count(), "No default routes installed") },
            { assertTrue(routes.any { it.path == "/status" && it.selector == RouteSelector.OPTIONS }, "Options status route not present") },
            { assertTrue(routes.any { it.path == "/status" && it.selector == RouteSelector.GET }, "Get status route not present") }
        )
    }

    @Test
    fun `test features can be switched off`() = testApplication {

        var corsPlugin :  PluginInstance? = null
        var contentNegotiationPlugin  :  PluginInstance? = null
        val routes :  MutableList<WraptorRoute> = mutableListOf()
        var configHit : Boolean = false

        val server =  RestWrapTor()

        val apiConfig = ApiConfig(
            cors = false,
            contentNegotiation = false,
            systemRouts = false
        )

        application{
            server.useApp(this){
                setup(WraptorConfig(EnvironmentType.BUILD, apiConfig)) {

                }
            }
            configHit = true
            routes.addAll(getWraptorRoutes())
            corsPlugin = this.pluginOrNull(CORS)
            contentNegotiationPlugin = this.pluginOrNull(ContentNegotiation)
        }
        startApplication()

        assertAll(
            {assertTrue(configHit, "Configuration never reached")},
            {assertNull(corsPlugin, "Cors plugin switched off but installed")},
            {assertNull(contentNegotiationPlugin, "ContentNegotiation plugin switched off but installed")}
        )

    }
}