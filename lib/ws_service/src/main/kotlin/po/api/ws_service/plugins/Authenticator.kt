package po.api.ws_service.service.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.request.ApplicationReceivePipeline
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.websocket.WebSockets
import io.ktor.util.AttributeKey
import po.api.rest_service.security.JWTService
import po.api.ws_service.WebSocketServer
import io.ktor.websocket.*


class ApiWebSocketsConfig() {
   lateinit var jwtService: JWTService
   private lateinit var server: WebSocketServer
}

private fun ApplicationReceivePipeline.intercept(value: Any) {}

class Authenticator(private val config: ApiWebSocketsConfig){

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, ApiWebSocketsConfig, Authenticator> {

        override val key = AttributeKey<Authenticator>("WSAuthenticator")
        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: ApiWebSocketsConfig.() -> Unit
        ): Authenticator {

            val  authenticator =  Authenticator(ApiWebSocketsConfig().apply(configure))
            return authenticator
        }
    }

}