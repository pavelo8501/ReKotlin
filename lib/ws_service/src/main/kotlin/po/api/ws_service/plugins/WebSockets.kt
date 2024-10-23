package po.api.ws_service.service.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.websocket.WebSockets
import io.ktor.util.AttributeKey


class ApiWebSocketsConfig(){
   lateinit  var hostWebSocket : WebSockets
}


class ApiWebSockets(private val config: WebSockets.WebSocketOptions){


    companion object Plugin : BaseApplicationPlugin<Application, WebSockets.WebSocketOptions, ApiWebSockets> {

        override val key = AttributeKey<ApiWebSockets>("ApiWebSockets")

        override fun install(
            pipeline: Application,
            configure: WebSockets.WebSocketOptions.() -> Unit
        ): ApiWebSockets {
          //  val webSocket =  pipeline.plugin(WebSockets)

            val apiWebSockets =  ApiWebSockets(WebSockets.WebSocketOptions().apply(configure))

           // apiWebSockets.config

          //  config.hostWebSocket.coroutineContext.


            return apiWebSockets
        }
    }

}