package po.wswraptor.plugins

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.receiveText
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.application
import io.ktor.util.AttributeKey
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import po.wswraptor.models.request.WSRequest

class ContentConverterConfig {
    internal val converters = mutableListOf<WebsocketContentConverter>()
    fun register(converter: WebsocketContentConverter) {
        converters.add(converter)
    }
}

val ContentConverterPlugin = createApplicationPlugin(
    name = "WebSocketApiContentConverter",
    createConfiguration = ::ContentConverterConfig
) {

    val DeserializedDataKey = AttributeKey<Any>("DeserializedData")

    val converters = pluginConfig.converters.toList()

    onCallReceive { call ->
        println(call.receiveText())
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
                typeInfo = typeInfo<WSRequest<Any>>(),
                content = content
            )
            if (result != null) {
                return result
            }
        }
        return null
    }

    fun interceptWebSocketSession(session: WebSocketServerSession, converters: List<WebsocketContentConverter>) {
        session.apply {
            launch {
                incoming.consumeAsFlow().collect { frame ->
                    if (frame is Frame.Text || frame is Frame.Binary) {
                        val result = processIncomingFrame(frame, converters, session)
                        if (result != null) {
                            session.application.attributes.put(DeserializedDataKey, result)
                        }
                    }
                }
            }
        }
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
