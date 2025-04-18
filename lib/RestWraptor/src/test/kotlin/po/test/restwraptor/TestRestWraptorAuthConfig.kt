package po.test.restwraptor

import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import po.auth.authentication.extensions.readCryptoRsaKeys
import po.auth.authentication.extensions.setKeyBasePath
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.models.AuthorizedPrincipal
import po.restwraptor.RestWrapTor
import po.restwraptor.models.server.WraptorRoute


@Serializable
data class TestUser(
    var id: Long,
    var name : String,
    override var login: String = "someLogin",
    var password : String = "somePassword",
    override var email: String = "some@mail.test",
    override var roles: Set<String> = setOf(),
) : AuthenticationPrincipal {

    override val userId: Long = id
    override val userGroupId: Long = 0

    fun initBy(authorized : AuthorizedPrincipal){
        id = authorized.userId
        login = authorized.login
        roles = authorized.roles
    }

    fun asAuthorizedPrincipal():AuthorizedPrincipal{
      return  AuthorizedPrincipal(id, login, email, userGroupId, roles)
    }

}


class TestRestWraptorAuthConfig {

    @Test
    fun `RSA key generation`(){
        // val keys = generateRsaKeys()
        // keys.writeToDisk("keys/")
    }

    suspend fun authenticate(login: String, password: String): AuthorizedPrincipal{
        return TestUser(0, "someName", login, password).asAuthorizedPrincipal()
    }

    suspend fun authenticateNotFound(login: String, password: String): AuthorizedPrincipal{
      throw Exception("Authentication failed")
    }

    @Test
    fun `JWT Token auth plugin installed and routes active`(){
        val keyPath = setKeyBasePath("src/test/demo_keys")
        val securedRoutes : MutableList<WraptorRoute> = mutableListOf()
        val server = RestWrapTor{
            setupAuthentication {

                jwtConfig(keyPath.readCryptoRsaKeys(
                    privateKeyFileName = "ktor.pk8",
                    publicKeyFileName = "ktor.spki"
                    )
                ){
                    setAuthenticationFn(::authenticate)
                }
            }
        }

        server.start{handler->
            securedRoutes.addAll(handler.getRoutes().filter { it.isSecured })
            handler.stop()
        }



    }

}