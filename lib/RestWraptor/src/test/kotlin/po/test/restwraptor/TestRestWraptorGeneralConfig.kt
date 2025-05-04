package po.test.restwraptor

import io.ktor.server.application.PluginInstance
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.readCryptoRsaKeys
import po.auth.extensions.setKeyBasePath
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.EnvironmentType
import po.restwraptor.enums.RouteSelector
import po.restwraptor.extensions.getWraptorRoutes
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.server.WraptorRoute
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestRestWraptorGeneralConfig {

    @Test
    fun `default configuration installs and configures all plugins`() = testApplication {

        val server = RestWrapTor()
        application {
            server.useApp(this){

            }
        }
        startApplication()
        val routes = server.getRoutes()
        val firsRoute = routes[0]
        assertAll(
            { assertNotEquals(0, routes.count(), "No default routes installed") },
            { assertEquals("/status", firsRoute.path, "Route path incorrect") },
            { assertEquals(RouteSelector.OPTIONS, firsRoute.selector, "Route path incorrect")},
            { assertNotNull(routes.firstOrNull { it.path.contains("status") && it.selector == RouteSelector.OPTIONS }, "Options status route not present") },
            { assertNotNull(routes.firstOrNull { it.path.contains("status") && it.selector == RouteSelector.GET }, "Get status route not present") },
            { assertNotNull(routes,"Routes not loaded")},
            { assertNotNull(routes.firstOrNull {it.path == "/backend/auth/login" && it.selector == RouteSelector.POST }, "backend/auth/login status route not present") }
        )
    }

    @Test
    fun `test features can be switched off`() = testApplication {

        var corsPlugin :  PluginInstance? = null
        var contentNegotiationPlugin  :  PluginInstance? = null
        var routes :  List<WraptorRoute>? = null
        var configHit : Boolean = false

        val server =  RestWrapTor()

        val apiConfig = ApiConfig(
            cors = false,
            contentNegotiation = false,
            systemRouts = false
        )
        val keyPath = setKeyBasePath("src/test/demo_keys")
        application{
            server.useApp(this){
                apiConfig.baseApiRoute = "backend/"
                apiConfig.environment = EnvironmentType.BUILD
                applyApiConfig(apiConfig)
            }

            configHit = true
            getWraptorRoutes(){
                routes = it
            }
            corsPlugin = this.pluginOrNull(CORS)
            contentNegotiationPlugin = this.pluginOrNull(ContentNegotiation)
        }
        startApplication()

        assertAll(
            {assertTrue(configHit, "Configuration never reached")},
            {assertNull(corsPlugin, "Cors plugin switched off but installed")},
            {assertNull(contentNegotiationPlugin, "ContentNegotiation plugin switched off but installed")},
            {assertNotNull(routes, "Route list not loaded")}
        )

    }
}