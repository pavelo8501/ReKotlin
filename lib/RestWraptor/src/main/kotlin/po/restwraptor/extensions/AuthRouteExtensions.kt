package po.restwraptor.extensions

import io.ktor.client.engine.callContext
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.engine.defaultEnginePipeline
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig

class AuthRouteExtensions {
}

public fun AuthenticationConfig.authCoreJwt(
    name: String? = null,
    service:JWTService,
    config: AuthenticationProvider.Config,
    configure: JWTAuthenticationProvider.Config.() -> Unit
) {


    register(object : AuthenticationProvider(config) {
        override suspend fun onAuthenticate(context: AuthenticationContext) {
            TODO("Not yet implemented")
        }
    })

}