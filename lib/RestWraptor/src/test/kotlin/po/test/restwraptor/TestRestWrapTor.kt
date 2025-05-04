package po.test.restwraptor

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.readCryptoRsaKeys
import po.auth.extensions.setKeyBasePath
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.RouteSelector
import po.restwraptor.extensions.jwtSecured
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.models.server.WraptorRoute
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@Serializable
data class TestResponse(
    val msg: String = "Some message",
    val number: Int = 10
)

class TestRestWrapTor {


    fun userLookUp(login: String): AuthenticationPrincipal {
        return TestUser(0, "someName", login)
    }
    private fun findRoute(routes: List<WraptorRoute>, predicate: (WraptorRoute) -> Boolean): Boolean{
       return routes.any{
           predicate.invoke(it)
       }
    }


    @Test
    fun `default route also applied to security routes`()  = testApplication {

        fun userLookUp(login: String): AuthenticationPrincipal?{
            if(login == "login"){
                return TestUser(0, "someName", "login")
            }else{
                return null
            }
        }
        val server =  RestWrapTor()
        val keyPath = setKeyBasePath("src/test/demo_keys")
        application{
            server.useApp(this){
                apiConfig.baseApiRoute = "backend/"
                setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::userLookUp)
            }
        }
        startApplication()
        val routes = server.getRoutes()
        assertNotNull(routes.firstOrNull{ it.path.contains("auth")}, "No security routes present")
        assertTrue("backend prefix not present"){ routes.any { it.path.contains("backend")} }
    }

    @Test
    fun `test server live run`() {
        val keyPath = setKeyBasePath("src/test/demo_keys")
        val securedRoutes: MutableList<WraptorRoute> = mutableListOf()
        var routesConfigured = false

        val server = RestWrapTor {
            setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::userLookUp)
            setupApplication {
                routesConfigured = true
                routing {
                    jwtSecured {
                        post("test"){

                        }
                    }
                }
            }
        }
        server.start { handler ->
            securedRoutes.addAll(handler.getRoutes())
            handler.stop()
        }

        fun check(predicate: (WraptorRoute) -> Boolean): Boolean{
            return securedRoutes.any{
                predicate.invoke(it)
            }
        }
        assertTrue("Routs not configured") { routesConfigured }
        assertTrue(check{ it.path.contains("test")  && it.isSecured  && it.selector == RouteSelector.POST},"Secured route not present",)
        assertNotEquals(0, securedRoutes.count(), "No default routes installed")
        assertTrue(check{it.path == "/auth/login" && it.selector == RouteSelector.POST }, "Post login route not present")
        assertTrue(check{ it.path == "/auth/refresh" && it.selector == RouteSelector.POST }, "Post refresh route not present")
    }
}