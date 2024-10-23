package po.api.ws_service.extensions

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.application
import io.ktor.server.application.plugin
import io.ktor.server.http.content.resource
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Route
import io.ktor.server.routing.intercept
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.util.AttributeKey
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketExtensionFactory
import io.ktor.websocket.WebSocketExtensionHeader
import io.ktor.websocket.readText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import po.api.rest_service.models.ApiRequest
import po.api.ws_service.CallAttributeKey
import po.api.ws_service.WebSocketMethodRegistryItem
import po.api.ws_service.WebSocketServer
import po.api.ws_service.models.WSApiRequest
import po.api.ws_service.webSocketMethodRegistryKey


class RouteNegotiator() {
    val someVal: String = "someval"
}

class RouteContentNegotiatorConfig() {
    lateinit var negotiator: RouteNegotiator
}

class RouteContent(config:RouteContentNegotiatorConfig):WebSocketExtension<RouteContentNegotiatorConfig>{



    companion object Extension : WebSocketExtensionFactory<RouteContentNegotiatorConfig,RouteContent> {
        /* Key to discover installed extension instance */
        override val key: AttributeKey<RouteContent> = AttributeKey("frame-logger")

        /** List of occupied rsv bits.
         * If the extension occupies a bit, it can't be used in other installed extensions. We use these bits to prevent plugin conflicts(prevent installing multiple compression plugins). If you're implementing a plugin using some RFC, rsv occupied bits should be referenced there.
         */
        override val rsv1: Boolean = false
        override val rsv2: Boolean = false
        override val rsv3: Boolean = false

        override fun install(config: RouteContentNegotiatorConfig.() -> Unit): RouteContent {
           val a = 10
           val config =  RouteContentNegotiatorConfig().apply(config)
           return RouteContent(config)
        }
    }

    override val factory: WebSocketExtensionFactory<RouteContentNegotiatorConfig, out WebSocketExtension<RouteContentNegotiatorConfig>> = RouteContent

    override val protocols: List<WebSocketExtensionHeader> = emptyList()
    override fun clientNegotiation(negotiatedProtocols: List<WebSocketExtensionHeader>): Boolean {
        return true
    }

    override fun serverNegotiation(requestedProtocols: List<WebSocketExtensionHeader>): List<WebSocketExtensionHeader> {
        return requestedProtocols
    }

    override fun processOutgoingFrame(frame: Frame): Frame {
        println("Process outgoing frame: $frame")
        return frame
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun processIncomingFrame(frame: Frame): Frame {

        val json = Json{
            ignoreUnknownKeys = true
            decodeEnumsCaseInsensitive = true
        }


        when(frame.frameType){

            FrameType.TEXT -> {
                val text =  (frame as Frame.Text).readText()
                val request = json.decodeFromString<WSApiRequest>(text)

               val a = 10
            }
            else -> {}
        }


        return frame
    }
}
