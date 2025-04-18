package po.restwraptor.plugins

import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import po.auth.authentication.jwt.JWTService


class JWTPluginConfig{

    lateinit var key: String
    lateinit var service: JWTService

    fun setup(key: String, service: JWTService) {
        this.key = key
        this.service = service
    }
}


val JWTPlugin: ApplicationPlugin<JWTPluginConfig> = createApplicationPlugin(
    name = "CallInterceptorPlugin",
    createConfiguration =  ::JWTPluginConfig
){

    val service = pluginConfig.service
    val key = pluginConfig.key

    application.install(Authentication) {
        jwt(key) {
            realm = service.realm
            verifier(service.getVerifier())
            validate { credential ->
                service.checkCredential(credential)
            }
        }
    }


}