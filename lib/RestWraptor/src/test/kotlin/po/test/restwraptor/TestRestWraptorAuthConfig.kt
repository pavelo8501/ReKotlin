package po.test.restwraptor

import org.junit.jupiter.api.Test
import po.auth.authentication.extensions.readRsaKeys
import po.auth.authentication.extensions.setKeyBasePath
import po.auth.sessions.models.AuthorizedPrincipal
import po.restwraptor.RestWrapTor
import po.restwraptor.models.server.WraptorRoute

class TestRestWraptorAuthConfig {

    @Test
    fun `JWT Token auth plugin installed and routes active`(){

      //  val basePath = setKeyBasePath("DemoKeys")

        val routes : MutableList<WraptorRoute> = mutableListOf()
        val server = RestWrapTor{
            setupAuthentication {
                setupJWTTokens(readRsaKeys("ktor.spki", "ktor.pk8")){
                    AuthorizedPrincipal()
                }
            }
        }

        server.start{handler->
            routes.addAll(handler.getRoutes())
            handler.stop()
        }


    }

}