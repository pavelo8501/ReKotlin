package po.restwraptor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey
import po.restwraptor.models.security.JwtConfig
import po.restwraptor.security.JWTService

class JWTPlugin(private val config: JwtConfig) {

    var jwtService: JWTService

    init {
        jwtService = JWTService()
    }

    companion object Plugin : BaseApplicationPlugin<Application, JwtConfig, JWTPlugin> {
        override val key = AttributeKey<JWTPlugin>("JwtPlugin")

        override fun install(pipeline: Application, configure: JwtConfig.() -> Unit): JWTPlugin {
            val config = JwtConfig().apply(configure)
            val jwtPlugin = JWTPlugin(config)
            jwtPlugin.jwtService.configure(config)
            return jwtPlugin
        }
    }

}