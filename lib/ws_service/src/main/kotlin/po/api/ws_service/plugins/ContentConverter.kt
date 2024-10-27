package po.api.ws_service.plugins

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.util.AttributeKey
import io.ktor.websocket.*
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.receiveText
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.application
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.w3c.dom.TypeInfo
import po.api.ws_service.service.models.ApiRequestDataType
import po.api.ws_service.service.models.WSApiRequest


class ContentConverterConfig {
    internal val converters = mutableListOf<WebsocketContentConverter>()

    fun register(converter: WebsocketContentConverter) {
        converters.add(converter)
    }
}

class WSApiContentConverter() {

    val DeserializedDataKey = AttributeKey<Any>("DeserializedData")

    fun create(): ApplicationPlugin<ContentConverterConfig> {

        val webSocketContentConverterPlugin = createApplicationPlugin(
            name = "WebSocketApiContentConverter",
            createConfiguration = ::ContentConverterConfig
        ) {

            val converters = pluginConfig.converters.toList()

            onCallReceive { call ->
               println(call.receiveText())
            }

            onCall { call ->
                if (call is WebSocketServerSession) {
                    val session = call
                    interceptWebSocketSession(session, converters)
                }
            }

            onCallReceive { call, content ->
                if (call is WebSocketServerSession) {
                    val session = call
                    if(content is Frame.Text) {
                        val result = processIncomingFrame(content, converters, session)
                        if (result != null) {
                            call.application.attributes.put(DeserializedDataKey, result)
                        }
                    }
                }
            }
        }
        return webSocketContentConverterPlugin
    }

    fun interceptWebSocketSession(session: WebSocketServerSession, converters: List<WebsocketContentConverter>) {
            session.apply {
                // Launch a coroutine to handle incoming frames
                launch {
                    incoming.consumeAsFlow().collect { frame ->
                        if (frame is Frame.Text || frame is Frame.Binary) {
                            // Intercept and process the frame
                            val result = processIncomingFrame(frame, converters, session)
                            // Handle the deserialized object (result) as needed
                            if (result != null) {
                                // You can, for example, store it in session attributes
                                session.application.attributes.put(DeserializedDataKey, result)
                            }
                        }
                    }
                }
            }
        }

    suspend fun processIncomingFrame(
        frame: Frame,
        converters: List<WebsocketContentConverter>,
        session: WebSocketServerSession
    ): Any? {
        val expectedType =  session.application.attributes.getOrNull(DeserializedDataKey)
            ?: return null // No expected type set

        val content = when (frame) {
            is Frame.Text -> return frame
            else -> return null
        }

        for (converter in converters) {
            val result = converter.deserialize(
                charset = Charsets.UTF_8,
                typeInfo = typeInfo<WSApiRequest<ApiRequestDataType>>(),
                content = content
            )
            if (result != null) {
                return result
            }
        }
        return null
    }

}





