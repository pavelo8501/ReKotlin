package po.playground.projects.ws_service

import io.ktor.server.application.install
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.parseWebSocketExtensions
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import po.api.rest_service.RestServer
import po.api.rest_service.common.SecureUserContext
import po.api.ws_service.WebSocketServer
import po.api.ws_service.apiWebSocket
import po.api.ws_service.apiWebSocketMethod
import java.io.File
import kotlin.io.readText


@Serializable
data class TestPartner(
    val name: String?  = null,
    val vat: Int? = null
)

@Serializable
data class WsUser(
    override val username: String,
    override val roles : List<String> = listOf<String>("user")
) :  SecureUserContext {
    override fun toPayload(): String {
       return Json.encodeToString<WsUser>(this)
    }
}


fun startWebSocketServer(host: String, port: Int) {

    val currentDir = File("").absolutePath

    RestServer.apiConfig.setAuthKeys(
        publicKey  =  File(currentDir+File.separator+"keys"+File.separator+"ktor.spki").readText(),
        privateKey =  File(currentDir+File.separator+"keys"+File.separator+"ktor.pk8").readText()
    )

    val wsServer =  WebSocketServer(){



        routing {
            apiWebSocket("/ws/apiTest"){
                apiWebSocketMethod<TestPartner>("partners"){

                    val a = 10
                }
            }
        }
    }
    wsServer.configureWSHost(host, 8080)
    wsServer.onLoginRequest = {
        it.username
        it.password

        WsUser("someUsername", listOf("user"))
    }

    wsServer.start(true)



}