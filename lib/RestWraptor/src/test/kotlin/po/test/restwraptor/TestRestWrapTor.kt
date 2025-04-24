package po.test.restwraptor

import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import po.auth.extensions.readCryptoRsaKeys
import po.auth.extensions.setKeyBasePath
import po.auth.sessions.models.AuthorizedPrincipal
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.RouteSelector
import po.restwraptor.models.server.WraptorRoute
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@Serializable
data class TestResponse(
    val msg: String = "Some message",
    val number: Int = 10
)

class TestRestWrapTor {


    suspend fun authenticate(login: String, password: String): AuthorizedPrincipal {
        return TestUser(0, "someName", login, password).asAuthorizedPrincipal()
    }

    private fun findRoute(routes: List<WraptorRoute>, predicate: (WraptorRoute) -> Boolean): Boolean{
       return routes.any{
           predicate.invoke(it)
       }
    }

    @Test
    fun `test server live run`() {
        val keyPath = setKeyBasePath("src/test/demo_keys")
        val securedRoutes: MutableList<WraptorRoute> = mutableListOf()
        val server = RestWrapTor {
            setupAuthentication(keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"), ::authenticate) {

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

        assertNotEquals(0, securedRoutes.count(), "No default routes installed")
        assertTrue(check{it.path == "/auth/login" && it.selector == RouteSelector.POST }, "Post login route not present")
        assertTrue(check{ it.path == "/auth/refresh" && it.selector == RouteSelector.POST }, "Post refresh route not present")
    }
}