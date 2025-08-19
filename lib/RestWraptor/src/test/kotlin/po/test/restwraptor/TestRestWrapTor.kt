package po.test.restwraptor

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
import po.restwraptor.configureWraptor
import po.restwraptor.enums.RouteSelector
import po.restwraptor.routes.jwtSecured
import po.restwraptor.models.server.WraptorRoute
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
            configureWraptor(server){
                setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::userLookUp)
            }
            rootPath = "backend/"
        }
        startApplication()
        val routes = server.getRoutes()
        assertNotNull(routes.firstOrNull{ it.path.contains("auth")}, "No security routes present")
        assertTrue("backend prefix not present"){ routes.any { it.path.contains("backend")} }
    }
}