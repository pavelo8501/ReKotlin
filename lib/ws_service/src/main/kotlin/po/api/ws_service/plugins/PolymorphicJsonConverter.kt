package po.api.ws_service.plugins

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining




import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import po.api.ws_service.models.WSApiRequest
import java.nio.charset.Charset
import kotlin.reflect.KType

class PolymorphicJsonConverter (private val json: Json) : WebsocketContentConverter{

    private val serializerCache = mutableMapOf<KType, KSerializer<Any>>()

    private fun getSerializerForType(type: KType): KSerializer<Any> {
        @Suppress("UNCHECKED_CAST")
        return serializerCache.getOrPut(type) {
            json.serializersModule.serializer(type) as KSerializer<Any>
        }
    }


    override suspend fun deserialize(
        charset: io.ktor.utils.io.charsets.Charset,
        typeInfo: TypeInfo,
        content: Frame
    ): Any? {
        if(content is Frame.Text){

            //val baseApiResponse = json.decodeFromString<WSApiRequest<Any>>(content.readText())
            val a = 10
        }
        return null
    }

    override fun isApplicable(frame: Frame): Boolean {
        TODO("Not yet implemented")
    }
}