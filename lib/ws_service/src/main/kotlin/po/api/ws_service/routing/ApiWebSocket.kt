package po.api.ws_service.service.routing

import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText



data class ApiWebSocket(
    val path: String,
    val protocol: String?,
    val registeredApiMethods : List<WebSocketMethod>
)


object ApiWebSocketClass : WebSocketMethodObserver{

    val webSocketMethods = mutableListOf<ApiWebSocket>()

    private val registeredApiMethods = mutableListOf<WebSocketMethod>()

    override fun onMethodInvoked(module: String, call: ApiWebSocketMethodContext.() -> Unit) {
        registeredApiMethods.add(WebSocketMethod(module, call))
        println("Method invoked for module: $module")
    }


    fun registerApiWebSocket(path: String, protocol: String?=null){
        webSocketMethods.add(ApiWebSocket(path,protocol, registeredApiMethods.toList()))
        registeredApiMethods.clear()
    }
}


fun  Routing.apiWebSocket(
    path: String,
    protocol: String? = null,
    body: DefaultWebSocketServerSession.() -> Unit) {
    val apiWebSocketClass : ApiWebSocketClass = ApiWebSocketClass

      webSocket(path, protocol) {
          body()
          apiWebSocketClass.registerApiWebSocket(path, protocol)
          println("apiWebSocket: New connection established on $path")
          /*  if (!call.request.headers.contains("X-My-Auth-Token")) {
                  close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
                  return@webSocket
              }*/
          for (frame in incoming) {
              when (frame) {
                  is Frame.Text -> {
                      val text = frame.readText()
                        println("apiWebSocket: Received: $text")

                  }
                  else -> {}
              }
          }
          try {

          } catch (e: Exception) {
              println("Error in WebSocket session: ${e.message}")
          } finally {
              println("apiWebSocket: Connection closed on $path")
          }
      }
  }

