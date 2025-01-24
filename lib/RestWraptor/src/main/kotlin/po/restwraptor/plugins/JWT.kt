package po.restwraptor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey
import po.restwraptor.security.JWTService
import po.restwraptor.security.JwtConfig

class JWTPlugin(private val jwtConfig: JwtConfig) {
    companion object Plugin : BaseApplicationPlugin<Application, JwtConfig, JWTPlugin> {
        override val key = AttributeKey<JWTPlugin>("JwtPlugin")

        override fun install(pipeline: Application, configure: JwtConfig.() -> Unit): JWTPlugin {
            val config = JwtConfig().apply(configure)
            val jwtService = JWTService().configure(config)
            val jwt = JWTPlugin(config)
            jwt.jwtService = jwtService
            return jwt
        }
    }
    var jwtService: JWTService? = null
}