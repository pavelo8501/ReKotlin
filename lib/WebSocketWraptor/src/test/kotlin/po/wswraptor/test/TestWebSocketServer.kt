package po.wswraptor.test

import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.webSocket
import org.junit.jupiter.api.BeforeAll
import po.wswraptor.WebSocketServer
import po.wswraptor.components.serializationfactory.SerializationFactory
import po.wswraptor.models.request.ApiRequestAction
import po.wswraptor.models.request.WSRequest
import po.wswraptor.models.response.WSResponse
import po.wswraptor.test.common.Test1
import po.wswraptor.test.common.Test2
import po.wswraptor.test.common.Test3
import po.wswraptor.webSocket
import kotlin.test.Test

class TestWebSocketServer {

    companion object {

        lateinit var wsServer : WebSocketServer

        @BeforeAll
        @JvmStatic
        fun setup()  = testApplication {
            application{
                wsServer = WebSocketServer(this)
            }

            wsServer.configure {
                routing {
                    webSocket("/partners", "partner") {

                    }
                }
            }
        }
    }


}