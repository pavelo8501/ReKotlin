package po.restwraptor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.util.AttributeKey
import po.restwraptor.models.security.JwtConfig
import po.restwraptor.security.JWTService

class JWTPlugin(private val service: JWTService) {

    companion object Plugin : BaseApplicationPlugin<Application, JWTService, JWTPlugin> {
        override val key = AttributeKey<JWTPlugin>("JwtPlugin")
        lateinit var service : JWTService

        override fun install(pipeline: Application,  serviceFn: JWTService.() -> Unit): JWTPlugin {
            this.service = JWTService()
            service.serviceFn()
            val jwtPlugin = JWTPlugin(service)
            pipeline.install(Authentication) {
                jwt(service.name) {
                    realm = service.realm
                    verifier(service.getVerifier())
                    validate { credential ->
                        return@validate  service.checkCredential(credential)
                    }
                }
            }
            return jwtPlugin
        }
    }
    fun getInitializedService(): JWTService{
        return this.service
    }

}