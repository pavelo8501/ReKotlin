package po.test.restwraptor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.RouteSelector
import po.restwraptor.models.server.WraptorRoute
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestRestWraptorGeneralConfig {

    @Test
    fun `default configuration installs and configures all plugins`(){

        val routes : MutableList<WraptorRoute> = mutableListOf()
        val server = RestWrapTor()
        server.start{handler->
            routes.addAll(handler.getRoutes())
            handler.stop()
        }

        assertAll(
            { assertNotEquals(0, routes.count(), "No default routes installed") },
            { assertTrue(routes.any { it.path == "/status" && it.selector == RouteSelector.OPTIONS }, "Options status route not present") },
            { assertTrue(routes.any { it.path == "/status" && it.selector == RouteSelector.GET }, "Get status route not present") }
        )
    }
}