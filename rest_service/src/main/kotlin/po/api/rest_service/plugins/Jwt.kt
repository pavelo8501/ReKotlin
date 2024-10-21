package po.api.rest_service.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.response.respondText
import io.ktor.util.AttributeKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import po.api.rest_service.security.JWTService
import po.api.rest_service.security.JwtConfig

class Jwt(private val jwtConfig: JwtConfig) {

    companion object Plugin : BaseApplicationPlugin<Application, JwtConfig, Jwt> {
        override val key = AttributeKey<Jwt>("JwtPlugin")

        override fun install(pipeline: Application, configure: JwtConfig.() -> Unit): Jwt {
            val config = JwtConfig().apply(configure)
            val jwtService = JWTService().configure(config)
            val jwt = Jwt(config)
            jwt.jwtService = jwtService
            return jwt
        }
    }
    var jwtService: JWTService? = null
}
