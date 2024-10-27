package po.api.ws_service.service.extensions

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.server.websocket.DefaultWebSocketServerSession
import po.api.ws_service.service.models.WSApiRequest
import io.ktor.util.AttributeKey
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketExtensionFactory
import io.ktor.websocket.WebSocketExtensionHeader
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.produce
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import po.api.ws_service.service.models.ApiRequestDataType
import po.api.ws_service.services.TrafficService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class TTConfig() {
    lateinit var service: TrafficService
    lateinit var contentConverter : WebsocketContentConverter
}



class TrafficController(config:TTConfig):WebSocketExtension<TTConfig>{
    val service = config
    companion object Extension : WebSocketExtensionFactory<TTConfig,TrafficController> {
        override val key: AttributeKey<TrafficController> = AttributeKey("frame-logger")

        /** List of occupied rsv bits.
         * If the extension occupies a bit, it can't be used in other installed extensions. We use these bits to prevent plugin conflicts(prevent installing multiple compression plugins). If you're implementing a plugin using some RFC, rsv occupied bits should be referenced there.
         */
        override val rsv1: Boolean = false
        override val rsv2: Boolean = false
        override val rsv3: Boolean = false

        override fun install(config: TTConfig.() -> Unit): TrafficController {
           val config =  TTConfig().apply(config)
           return TrafficController(config)
        }
    }

    override val factory: WebSocketExtensionFactory<TTConfig, out WebSocketExtension<TTConfig>> = TrafficController

    override val protocols: List<WebSocketExtensionHeader> = emptyList()
    override fun clientNegotiation(negotiatedProtocols: List<WebSocketExtensionHeader>): Boolean {
        return true
    }

    override fun serverNegotiation(requestedProtocols: List<WebSocketExtensionHeader>): List<WebSocketExtensionHeader> {
        return requestedProtocols
    }

    override fun processOutgoingFrame(frame: Frame): Frame {
       // println("Process outgoing frame: $frame")
        return frame
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun processIncomingFrame(frame: Frame): Frame {
       // println("Process Incoming frame: $frame")
        return frame
    }
}
